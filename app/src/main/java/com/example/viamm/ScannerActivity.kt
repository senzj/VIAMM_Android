package com.example.viamm

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.SurfaceTexture
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.view.Surface
import android.view.TextureView
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.viamm.loadings.LoadingDialog
import com.example.viamm.loadings.MainLoading
import com.example.viamm.ml.Detect
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.common.FileUtil
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.util.Arrays
import kotlin.math.exp

class ScannerActivity : AppCompatActivity() {

    lateinit var labels: List<String>
    lateinit var imageProcessor: ImageProcessor
    lateinit var bitmap: Bitmap
    lateinit var imageView: ImageView
    lateinit var cameraDevice: CameraDevice
    lateinit var handler: Handler
    lateinit var textureView: TextureView
    lateinit var cameraManager: CameraManager
    lateinit var model: MyDetector
    val paint = Paint()
    var colors = listOf(
        Color.BLUE, Color.GREEN, Color.RED, Color.CYAN, Color.GRAY, Color.BLACK,
        Color.DKGRAY, Color.MAGENTA, Color.YELLOW, Color.RED
    )
    // for loading screen dialog
    private lateinit var loadingDialog: MainLoading


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContentView(R.layout.activity_scanner)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        // setting loading dialog..
        loadingDialog = MainLoading(this)
        loadingDialog.show()

        // getting permissions for the camera
        getPermission()

        val handlerThread = HandlerThread("videoThread")
        handlerThread.start()

        // Load the labels from the file
        labels = FileUtil.loadLabels(this, "labelmap.txt")

        // Loads the TensorFlow Lite model
        imageProcessor = ImageProcessor.Builder().add(ResizeOp(640, 640, ResizeOp.ResizeMethod.BILINEAR)).build()

        // Use your custom wrapper class
        model = MyDetector(this)

        // Initialize the ImageView and TextureView
        handler = Handler(handlerThread.looper)

        // Initialize the ImageView and TextureView
        imageView = findViewById(R.id.imageView)

        // Initialize the TextureView
        textureView = findViewById(R.id.textureView)

        // Set the TextureView surface texture listener
        textureView.surfaceTextureListener = object : TextureView.SurfaceTextureListener {

            // if surface is available, open camera
            override fun onSurfaceTextureAvailable(p0: SurfaceTexture, p1: Int, p2: Int) {
                openCamera()
            }

            // if surface is changed, do
            override fun onSurfaceTextureSizeChanged(p0: SurfaceTexture, p1: Int, p2: Int) {

            }

            // if surface is destroyed, do nothing
            override fun onSurfaceTextureDestroyed(p0: SurfaceTexture): Boolean {
                return false
            }

            // if surface is updated, do
            override fun onSurfaceTextureUpdated(surfaceTexture: SurfaceTexture) {
                // Get the latest frame from the texture view
                val bitmap = textureView.bitmap!!

                // Preprocess the image
                var image = TensorImage.fromBitmap(bitmap)
                image = imageProcessor.process(image)

                // Log resized dimensions and buffer information
                Log.d("Tensor", "onSurfaceTextureUpdated: Resized width = ${image.width}, Resized height = ${image.height}, Buffer size = ${image.buffer.remaining()}, Image size after resizing = (${image.width} x ${image.height})")

                // Verify buffer content
                val buffer = image.buffer
                val expectedBufferSize = 320 * 320 * 3 * 4 // 320x320 image, 3 channels (RGB), 4 bytes per float
                Log.d("Tensor", "Buffer: ${buffer.toString()}")

                // Check if the buffer size matches the expected size
                if (buffer.remaining() != expectedBufferSize) {
                    Log.e("Tensor", "Buffer size mismatch: expected $expectedBufferSize but got ${buffer.remaining()}")
                    return
                }

                // Ensure the buffer position is at the start
                buffer.rewind()

                // Load the preprocessed image into the TensorBuffer
                val inputFeature0 = TensorBuffer.createFixedSize(intArrayOf(1, 320, 320, 3), DataType.FLOAT32)
                try {
                    inputFeature0.loadBuffer(buffer)
                } catch (e: IllegalArgumentException) {
                    Log.e("Tensor", "Exception: ${e.message}")
                    return
                }

                // Run model inference using the custom wrapper
                val detectionResults = model.process(inputFeature0)

                // Draw the results on the canvas
                val mutableBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
                val canvas = Canvas(mutableBitmap)
                val h = mutableBitmap.height
                val w = mutableBitmap.width
                paint.textSize = h / 15f
                paint.strokeWidth = h / 85f

                detectionResults.scores.forEachIndexed { index, score ->
                    if (score > 0.5) {
                        val locIndex = index * 4
                        val left = detectionResults.locations[locIndex + 1] * w
                        val top = detectionResults.locations[locIndex] * h
                        val right = detectionResults.locations[locIndex + 3] * w
                        val bottom = detectionResults.locations[locIndex + 2] * h

                        paint.color = colors[index % colors.size]
                        paint.style = Paint.Style.STROKE
                        canvas.drawRect(RectF(left, top, right, bottom), paint)
                        paint.style = Paint.Style.FILL
                        canvas.drawText(
                            "${labels[detectionResults.classProbabilities[index].toInt()]} ${score.toString()}",
                            left,
                            top,
                            paint
                        )
                    }
                }

                imageView.setImageBitmap(mutableBitmap)
            }
        }

        cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
    }

    override fun onDestroy() {
        super.onDestroy()
        model.close()
    }

    private fun getPermission() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(android.Manifest.permission.CAMERA), 101)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
            getPermission()
        }
    }

    // getPermission() already checks for permission, this just removes the error for "not" having perms
    @SuppressLint("MissingPermission")
    fun openCamera() {
        cameraManager.openCamera(cameraManager.cameraIdList[0], object : CameraDevice.StateCallback() {

            override fun onOpened(p0: CameraDevice) {
                cameraDevice = p0

                val surfaceTexture = textureView.surfaceTexture
                val surface = Surface(surfaceTexture)

                val captureRequest = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
                captureRequest.addTarget(surface)

                cameraDevice.createCaptureSession(listOf(surface), object : CameraCaptureSession.StateCallback() {

                    override fun onConfigured(p0: CameraCaptureSession) {
                        p0.setRepeatingRequest(captureRequest.build(), null, null)
                        Log.d("ScannerActivity", "Camera Connected")
                        
                        // Dismiss loading dialog
                        loadingDialog.dismiss()
                    }

                    override fun onConfigureFailed(p0: CameraCaptureSession) {
                        Log.e("ScannerActivity", "onConfigureFailed")
                    }

                }, null)
            }

            override fun onDisconnected(p0: CameraDevice) {
                Log.d("ScannerActivity", "Camera Disconnected")
            }

            override fun onError(p0: CameraDevice, p1: Int) {
                Log.d("ScannerActivity", "Camera Error")
            }

        }, handler)
    }

    // Custom wrapper class for the TensorFlow Lite model
    inner class MyDetector(context: Context) {
        private val detect = Detect.newInstance(context)

        fun process(inputFeature0: TensorBuffer): DetectionResults {
            // Run inference using the generated class
            val outputs = detect.process(inputFeature0)
            val locations = outputs.outputFeature0AsTensorBuffer.floatArray
            val classes = outputs.outputFeature1AsTensorBuffer.floatArray
            val scores = outputs.outputFeature2AsTensorBuffer.floatArray
            val numberOfDetections = outputs.outputFeature3AsTensorBuffer.floatArray

            // Log the raw model output
            Log.d("TensorFlow", "Model Output - Feature0: ${outputs.outputFeature0AsTensorBuffer.dataType}")
            Log.d("TensorFlow", "Model Output - Feature1: ${outputs.outputFeature1AsTensorBuffer.dataType}")
            Log.d("TensorFlow", "Model Output - Feature2: ${outputs.outputFeature2AsTensorBuffer.dataType}")
            Log.d("TensorFlow", "Model Output - Feature3: ${outputs.outputFeature3AsTensorBuffer.dataType}")

            Log.d("TensorFlow", "Model Output - Feature 0 Shape: ${Arrays.toString(outputs.outputFeature0AsTensorBuffer.shape)}")
            Log.d("TensorFlow", "Model Output - Feature 1 Shape: ${Arrays.toString(outputs.outputFeature1AsTensorBuffer.shape)}")
            Log.d("TensorFlow", "Model Output - Feature 2 Shape: ${Arrays.toString(outputs.outputFeature2AsTensorBuffer.shape)}")
            Log.d("TensorFlow", "Model Output - Feature 3 Shape: ${Arrays.toString(outputs.outputFeature3AsTensorBuffer.shape)}")

            Log.d("TensorFlow", "Model Output - Feature0 Sample: ${Arrays.toString(outputs.outputFeature0AsTensorBuffer.floatArray.take(5).toFloatArray())}")
            Log.d("TensorFlow", "Model Output - Feature1 Sample: ${Arrays.toString(outputs.outputFeature1AsTensorBuffer.floatArray.take(5).toFloatArray())}")
            Log.d("TensorFlow", "Model Output - Feature2 Sample: ${Arrays.toString(outputs.outputFeature2AsTensorBuffer.floatArray.take(5).toFloatArray())}")
            Log.d("TensorFlow", "Model Output - Feature3 Sample: ${Arrays.toString(outputs.outputFeature3AsTensorBuffer.floatArray.take(5).toFloatArray())}")

            Log.d("TensorFlow", "Model Output - Locations: ${locations.contentToString()}")
            Log.d("TensorFlow", "Model Output - Classes: ${classes.contentToString()}")
            Log.d("TensorFlow", "Model Output - Scores: ${scores.contentToString()}")
            Log.d("TensorFlow", "Model Output - Number of Detections: ${numberOfDetections.contentToString()}")

            // Apply softmax to class probabilities
            val classProbabilities = softmax(classes)

            // Derive score from class probabilities (adjust as needed)
            val score = classProbabilities.maxOrNull() ?: 1.0f

            // Return the detection results
            return DetectionResults(locations, classProbabilities, score, scores, numberOfDetections)
        }

        // Softmax function (implement for probability calculation)
        private fun softmax(logits: FloatArray): FloatArray {
            val expValues = logits.map { exp(it) }
            val sumExp = expValues.sum()
            return expValues.map { it / sumExp }.toFloatArray()
        }

        fun close() {
            detect.close()
        }
    }

    // Data class to hold detection results
    data class DetectionResults(
        val locations: FloatArray,
        val classProbabilities: FloatArray,
        val score: Float,
        val scores: FloatArray,
        val numberOfDetections: FloatArray
    )
}