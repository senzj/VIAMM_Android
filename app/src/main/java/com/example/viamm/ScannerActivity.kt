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
import org.tensorflow.lite.DataType
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.gpu.CompatibilityList
import org.tensorflow.lite.gpu.GpuDelegate
import org.tensorflow.lite.support.common.FileUtil
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer

class ScannerActivity : AppCompatActivity() {

    lateinit var imageProcessor: ImageProcessor
    lateinit var bitmap: Bitmap
    lateinit var imageView: ImageView
    lateinit var textureView: TextureView
    lateinit var cameraManager: CameraManager
    lateinit var cameraDevice: CameraDevice
    lateinit var handler: Handler
    lateinit var interpreter: Interpreter
    lateinit var labels: List<String>

    var colors = listOf(
        Color.BLUE, Color.GREEN, Color.RED, Color.CYAN, Color.GRAY, Color.BLACK,
        Color.DKGRAY, Color.MAGENTA, Color.YELLOW, Color.RED
    )
    val paint = Paint()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_scanner)

        // Load labels
        labels = FileUtil.loadLabels(this, "labelmap.txt")

        // Load model
        val modelFile = FileUtil.loadMappedFile(this, "detect.tflite")
        interpreter = Interpreter(modelFile)

        //GPU delagation for performance
        try {
            val compatList = CompatibilityList()
            val options = if(compatList.isDelegateSupportedOnThisDevice) {
                Log.d("GPU Delegate", "Using GPU delegate")
                val delegateOptions = compatList.bestOptionsForThisDevice
                val gpuDelegate = GpuDelegate(delegateOptions)
                Interpreter.Options().addDelegate(gpuDelegate)
            } else {
                Log.d("GPU Delegate", "GPU delegate not supported. Using CPU.")
                Interpreter.Options()
            }
            interpreter = Interpreter(modelFile, options)
        } catch (e: Exception) {
            Log.e("GPU Delegate", "Error creating interpreter: ${e.message}")
            // Handle error, e.g., fallback to CPU inference
        }

        // Image processor
        imageProcessor = ImageProcessor.Builder()
            .add(ResizeOp(320, 320, ResizeOp.ResizeMethod.BILINEAR))
            .build()

        val handlerThread = HandlerThread("videoThread")
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

                // Log ImageView dimensions
                Log.d("ImageView Dimensions", "Width: ${imageView.width}, Height: ${imageView.height}")

                // Calculate scaling factor based on smaller dimension
                val scaleFactor = minOf(imageView.width / 320.0f, imageView.height / 320.0f)

                // Calculate aspect ratio scaling factors
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

                    // Correctly sized output buffers
                    val outputLocations = TensorBuffer.createFixedSize(intArrayOf(1, 10, 4), DataType.FLOAT32) // 10 detections, each with 4 coordinates
                    val outputClasses = TensorBuffer.createFixedSize(intArrayOf(1, 10), DataType.FLOAT32) // 10 detection classes
                    val outputScores = TensorBuffer.createFixedSize(intArrayOf(1, 10), DataType.FLOAT32) // 10 detection scores
                    val outputDetections = TensorBuffer.createFixedSize(intArrayOf(1, 10), DataType.FLOAT32) // Number of detections

                    // Run inference
                    interpreter.run(inputBuffer.buffer, outputLocations.buffer)
                    interpreter.run(inputBuffer.buffer, outputClasses.buffer)
                    interpreter.run(inputBuffer.buffer, outputScores.buffer)
                    interpreter.run(inputBuffer.buffer, outputDetections.buffer)

                    val locations = outputLocations.floatArray
                    val classes = outputClasses.floatArray
                    val scores = outputScores.floatArray
                    val numberOfDetections = outputDetections.floatArray[0] // Get number of detections as an integer

                    Log.d("TENSOR LOG", "Number of Detections: $numberOfDetections")

                    // Create a mutable bitmap for drawing
                    val mutable = bitmap.copy(Bitmap.Config.ARGB_8888, true)
                    val canvas = Canvas(mutable)

                    // Draw bounding boxes and labels
                    val h = mutable.height
                    val w = mutable.width
                    paint.textSize = h / 15f
                    paint.strokeWidth = h / 150f

                    for (i in 0 until scores.size) {

                        val score = scores[i]

                        if (score > 0.35 && i != 0) {
                            val x = i * 4
                            val ymin = locations[x]
                            val xmin = locations[x + 1]
                            val ymax= locations[x + 2]
                            val xmax = locations[x + 3]

                            val boxLeft = (xmin * scaleFactor * imageView.width)
                            val boxTop = (ymin * scaleFactor * imageView.height)
                            val boxRight = (xmax * scaleFactor * imageView.width)
                            val boxBottom = (ymax * scaleFactor * imageView.height)

                            Log.d("Bounding Coordinates (Original)", "xmin: $xmin, ymin: $ymin, xmax: $xmax, ymax: $ymax")
                            Log.d("Bounding Coordinates", "boxLeft: $boxLeft")
                            Log.d("Bounding Coordinates", "boxTop: $boxTop")
                            Log.d("Bounding Coordinates", "boxRight: $boxRight")
                            Log.d("Bounding Coordinates", "boxBottom: $boxBottom")

                            paint.color = colors[i % colors.size]
                            paint.style = Paint.Style.STROKE
                            canvas.drawRect(
                                RectF(
                                    boxLeft, boxTop,
                                    boxRight, boxBottom
                                ), paint
                            )
                            paint.style = Paint.Style.FILL
                            canvas.drawText(
                                "${labels[classes[i].toInt()]} %.2f%%".format(score * 100),
                                boxLeft, boxTop + 10, paint // Adjust vertical position
                            )
                            paint.color = Color.RED
                            paint.style = Paint.Style.FILL
                            canvas.drawCircle(imageView.width / 2.0f, imageView.height / 2.0f, 10f, paint) // Center
                            canvas.drawCircle(0f, 0f, 10f, paint) // Top-left corner

                        }

                    }
                    for (i in 0 until scores.size) {
                        val score = scores[i]
                        val classIndex = classes[i].toInt()
                        val className = if (classIndex in labels.indices) labels[classIndex] else "Unknown"

                        Log.d("Detection", "Object $i: Class=$className, Score=$score")
                    }

                    // Display annotated bitmap in the ImageView
                    runOnUiThread {
                        imageView.setImageBitmap(mutable)
                    }
                }
            }

        }

        cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
    }

    @SuppressLint("MissingPermission") //ALREADY HAS PERMISSION FROM MAIN ACTIVITY
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
            }

            override fun onDisconnected(p0: CameraDevice) {}

            override fun onError(p0: CameraDevice, p1: Int) {}
        }, handler)
    }
}
