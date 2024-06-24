package com.example.viamm

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
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
import com.example.viamm.loadings.MainLoading
import org.tensorflow.lite.DataType
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.Tensor
import org.tensorflow.lite.gpu.CompatibilityList
import org.tensorflow.lite.gpu.GpuDelegate
import org.tensorflow.lite.support.common.FileUtil
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.nio.ByteBuffer

class ScannerActivity : AppCompatActivity() {

    lateinit var imageProcessor: ImageProcessor
    lateinit var bitmap: Bitmap
    lateinit var imageView: ImageView
    lateinit var textureView: TextureView
    lateinit var cameraManager: CameraManager
    lateinit var cameraDevice: CameraDevice
    lateinit var handler: Handler
    lateinit var handlerThread: HandlerThread
    lateinit var interpreter: Interpreter
    lateinit var labels: List<String>
    private lateinit var loadingDialog: MainLoading

    var colors = listOf(
        Color.BLUE, Color.GREEN, Color.RED, Color.CYAN, Color.GRAY, Color.BLACK,
        Color.DKGRAY, Color.MAGENTA, Color.YELLOW, Color.RED
    )
    val paint = Paint()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_scanner)

        // Initialize loading dialog
        loadingDialog = MainLoading(this)
        loadingDialog.show()

        // Load labels
        labels = FileUtil.loadLabels(this, "labelmap.txt")

        // Load model
        val modelFile = FileUtil.loadMappedFile(this, "detect.tflite")
        interpreter = Interpreter(modelFile)

        // Image processor
        imageProcessor = ImageProcessor.Builder()
            .add(ResizeOp(320, 320, ResizeOp.ResizeMethod.BILINEAR))
            .build()

        handlerThread = HandlerThread("videoThread")
        handlerThread.start()
        handler = Handler(handlerThread.looper)

        imageView = findViewById(R.id.imageView)
        textureView = findViewById(R.id.textureView)
        textureView.surfaceTextureListener = object : TextureView.SurfaceTextureListener {
            override fun onSurfaceTextureAvailable(p0: SurfaceTexture, p1: Int, p2: Int) {
                openCamera()
            }

            override fun onSurfaceTextureSizeChanged(p0: SurfaceTexture, p1: Int, p2: Int) {}
            override fun onSurfaceTextureDestroyed(p0: SurfaceTexture): Boolean {
                return false
            }

            override fun onSurfaceTextureUpdated(surfaceTexture: SurfaceTexture) {
                moneyScanner()
            }
        }

        cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
    }

    private fun getTensorBufferUsingReflection(tensor: Tensor): ByteBuffer {
        val bufferMethod = Tensor::class.java.getDeclaredMethod("buffer")
        bufferMethod.isAccessible = true
        return bufferMethod.invoke(tensor) as ByteBuffer
    }

    @SuppressLint("MissingPermission") // ALREADY HAS PERMISSION FROM MAIN ACTIVITY
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
                    }

                    override fun onConfigureFailed(p0: CameraCaptureSession) {}
                }, handler)

                // Dismiss the loading dialog
                loadingDialog.dismiss()
            }

            override fun onDisconnected(p0: CameraDevice) {
                cameraDevice.close()
            }

            override fun onError(p0: CameraDevice, p1: Int) {
                cameraDevice.close()
            }

        }, handler)
    }

    // Pauses the scanner activity, such as close app or switch tabs
    override fun onPause() {
        super.onPause()
        closeCamera()
        Log.d("ScannerActivity", "Scanner Activity Paused")
    }

    // Stops the scanner activity, such as close app or switch tabs
    override fun onStop() {
        super.onStop()
        closeCamera()
        Log.d("ScannerActivity", "Scanner Activity Stopped")
    }

    // Resumes the scanner activity
    override fun onResume() {
        super.onResume()
        if (textureView.isAvailable) {
            openCamera()
        } else {
            textureView.surfaceTextureListener = object : TextureView.SurfaceTextureListener {

                override fun onSurfaceTextureAvailable(p0: SurfaceTexture, p1: Int, p2: Int) {
                    openCamera()
                    Log.d("ScannerActivity", "Scanner Camera Opened")
                }

                override fun onSurfaceTextureSizeChanged(p0: SurfaceTexture, p1: Int, p2: Int) {
                }

                override fun onSurfaceTextureDestroyed(p0: SurfaceTexture): Boolean {
                    return false
                }

                override fun onSurfaceTextureUpdated(surfaceTexture: SurfaceTexture) {
                    moneyScanner()
                }


            }
        }
        Log.d("ScannerActivity", "Scanner Activity Resumed")
    }

    fun moneyScanner (){
        // Calculate scaling factor based on smaller dimension
        val scaleFactor = minOf(imageView.width / 320.0f, imageView.height / 320.0f)

        val currentBitmap = textureView.bitmap
        if (currentBitmap != null) {
            bitmap = currentBitmap // Only assign if not null

            // Preprocess the bitmap
            var image = TensorImage(DataType.FLOAT32)
            image.load(bitmap)

            // Normalize pixel values manually
            val buffer = image.tensorBuffer.buffer
            while (buffer.hasRemaining()) {
                buffer.putFloat(buffer.getFloat() / 255.0f)
            }

            image = imageProcessor.process(image)

            // Prepare input tensor
            val inputBuffer = TensorBuffer.createFixedSize(intArrayOf(1, 320, 320, 3), DataType.FLOAT32)
            inputBuffer.loadBuffer(image.tensorBuffer.buffer)

            // Allocate output buffers with appropriate sizes
            val outputLocations = TensorBuffer.createFixedSize(intArrayOf(1, 10, 4), DataType.FLOAT32) // Bounding box locations
            val outputClasses = TensorBuffer.createFixedSize(intArrayOf(1, 10, 19), DataType.FLOAT32) // Detection classes
            val outputScores = TensorBuffer.createFixedSize(intArrayOf(1, 10), DataType.FLOAT32) // Detection scores
            val outputDetections = TensorBuffer.createFixedSize(intArrayOf(1, 10), DataType.FLOAT32) // Number of detections

            // Prepare a map for the output buffers
            val outputMap = mapOf(
                0 to outputScores.buffer, // Detection scores (identifier 339)
                1 to outputClasses.buffer, // Detection classes (identifier 338)
                2 to outputLocations.buffer, // Detection boxes (identifier 337)
                3 to outputDetections.buffer // Number of detections (identifier 340)
            )

            // Run inference
            interpreter.runForMultipleInputsOutputs(arrayOf(inputBuffer.buffer), outputMap)

            val detectionBoxesTensor = interpreter.getOutputTensor(1)
            val rawLocationsBuffer = getTensorBufferUsingReflection(detectionBoxesTensor) // Pass the Tensor object
            val rawLocationsBytes = ByteArray(rawLocationsBuffer.remaining())
            rawLocationsBuffer.get(rawLocationsBytes)

            // Extract output data
            val coordinatesPerDetection = 4
            val scores = outputScores.floatArray
            val classes = Array(outputClasses.shape[1]) { IntArray(outputClasses.shape[2]) }

            Log.d("TENSOR LOG", "Scores: ${outputScores.floatArray.joinToString(", ")}")
            Log.d("TENSOR LOG", "Classes: ${classes.map { it[0] }.joinToString(", ")}") // Extract top class indices
            Log.d("TENSOR LOG", "Number of Detections: ${outputDetections.floatArray.joinToString(", ")}")
            Log.d("TENSOR LOG", "Raw Locations (Bytes): ${rawLocationsBytes.joinToString(", ")}")

            for (i in 0 until outputClasses.shape[1]) {
                val classProbabilities = outputClasses.floatArray.sliceArray(i* outputClasses.shape[2] until (i + 1) * outputClasses.shape[2])
                val maxIndex = classProbabilities.indices.maxByOrNull { classProbabilities[it] } ?: -1
                classes[i][0] = maxIndex
            }

            val scoreThreshold = 0.075f // Adjust this as needed

            val numDetections = scores.count { it > scoreThreshold }

            // Create a mutable bitmap for drawing
            val mutable = bitmap.copy(Bitmap.Config.ARGB_8888, true)
            val canvas = Canvas(mutable)

            // Draw bounding boxes and labels
            val h = mutable.height
            val w = mutable.width
            paint.textSize = h / 15f
            paint.strokeWidth = h / 150f

            for (i in 0 until numDetections) {

                val score = scores[i]

                val startIndex = i * coordinatesPerDetection
                // Check if enough coordinates are available
                if (startIndex + 3 < rawLocationsBytes.size && i < classes.size) {
                    val ymin = (rawLocationsBytes[startIndex].toInt() and 0xFF) / 255.0f
                    val xmin = (rawLocationsBytes[startIndex + 1].toInt() and 0xFF) / 255.0f
                    val ymax = (rawLocationsBytes[startIndex + 2].toInt() and 0xFF) / 255.0f
                    val xmax = (rawLocationsBytes[startIndex + 3].toInt() and 0xFF) / 255.0f

                    val imageWidth = bitmap.width
                    val imageHeight = bitmap.height

                    // Calculate box dimensions based on normalized coordinates
                    val boxLeft = xmin * imageWidth
                    val boxTop = ymin * imageHeight
                    val boxRight = xmax * imageWidth
                    val boxBottom = ymax * imageHeight

                    paint.color = colors[i % colors.size]
                    paint.style = Paint.Style.STROKE
                    canvas.drawRect(
                        RectF(
                            boxLeft, boxTop,
                            boxRight, boxBottom
                        ), paint
                    )
                    paint.style = Paint.Style.FILL
                    val predictedClassIndex = classes[i][0]
                    val className = if (predictedClassIndex in labels.indices) labels[predictedClassIndex] else "Unknown"
                    canvas.drawText("$className  %.2f%%".format(score * 100), boxRight, boxTop, paint)
                }
            }

            // Display annotated bitmap in the ImageView
            runOnUiThread {
                imageView.setImageBitmap(mutable)
            }
        }
    }

    // Closes the opened scanner activity
    override fun onDestroy() {
        super.onDestroy()
        handlerThread.quitSafely()
        interpreter.close()
        Log.d("ScannerActivity", "Scanner Activity Destroyed")
    }

    // Closes the camera if not used
    private fun closeCamera() {
        if (::cameraDevice.isInitialized) {
            cameraDevice.close()
            Log.d("ScannerActivity_Camera", "Camera Closed")
        }
    }

}
