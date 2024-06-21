package com.example.viamm

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
<<<<<<< Updated upstream

class ScannerActivity : AppCompatActivity() {
=======
import org.tensorflow.lite.DataType
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer


class ScannerActivity : AppCompatActivity() {

    lateinit var labels: List<String>
    lateinit var imageProcessor: ImageProcessor
    lateinit var imageView: ImageView
    lateinit var cameraDevice: CameraDevice
    lateinit var handler: Handler
    lateinit var textureView: TextureView
    lateinit var cameraManager: CameraManager
    lateinit var interpreter: Interpreter

    var colors = listOf(
        Color.BLUE, Color.GREEN, Color.RED, Color.CYAN, Color.GRAY, Color.BLACK,
        Color.DKGRAY, Color.MAGENTA, Color.YELLOW, Color.RED
    )
    val paint = Paint()

>>>>>>> Stashed changes
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_scanner)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
<<<<<<< Updated upstream
    }
}
=======
        getPermission()

        labels = FileUtil.loadLabels(this, "labelmap.txt")

        imageProcessor = ImageProcessor.Builder().add(ResizeOp(320, 320, ResizeOp.ResizeMethod.BILINEAR)).build()

        // Load the model and initialize the interpreter
        val model = FileUtil.loadMappedFile(this, "detect.tflite")
        interpreter = Interpreter(model)

        val handlerThread = HandlerThread("videoThread")
        handlerThread.start()
        handler = Handler(handlerThread.looper)

        imageView = findViewById(R.id.imageView)

        textureView = findViewById(R.id.textureView)
        textureView.surfaceTextureListener = object : TextureView.SurfaceTextureListener {
            override fun onSurfaceTextureAvailable(p0: SurfaceTexture, p1: Int, p2: Int) {
                open_camera()
            }

            override fun onSurfaceTextureSizeChanged(p0: SurfaceTexture, p1: Int, p2: Int) {}

            override fun onSurfaceTextureDestroyed(p0: SurfaceTexture): Boolean {
                return false
            }

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

                // Prepare output buffers
                val outputLocations = Array(1) { FloatArray(1 * 10 * 4) }
                val outputClasses = Array(1) { FloatArray(1 * 10) }
                val outputScores = Array(1) { FloatArray(1 * 10) }
                val outputNumberOfDetections = FloatArray(1)

                // Run model inference
                val inputArray = arrayOf(inputFeature0.buffer)
                val outputMap = mapOf(
                    0 to outputLocations,
                    1 to outputClasses,
                    2 to outputScores,
                    3 to outputNumberOfDetections
                )
                interpreter.runForMultipleInputsOutputs(inputArray, outputMap)

                // Extract the results
                val locations = outputLocations[0]
                val classes = outputClasses[0]
                val scores = outputScores[0]
                val numberOfDetections = outputNumberOfDetections[0]

                // Draw the results on the canvas
                val mutableBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
                val canvas = Canvas(mutableBitmap)
                val h = mutableBitmap.height
                val w = mutableBitmap.width
                paint.textSize = h / 15f
                paint.strokeWidth = h / 85f

                for (i in 0 until numberOfDetections.toInt()) {
                    if (scores[i] > 0.5) {
                        val locIndex = i * 4
                        val left = locations[locIndex + 1] * w
                        val top = locations[locIndex] * h
                        val right = locations[locIndex + 3] * w
                        val bottom = locations[locIndex + 2] * h

                        paint.color = colors[i % colors.size]
                        paint.style = Paint.Style.STROKE
                        canvas.drawRect(RectF(left, top, right, bottom), paint)
                        paint.style = Paint.Style.FILL
                        canvas.drawText(
                            "${labels[classes[i].toInt()]} ${scores[i]}",
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
        interpreter.close()
    }

    private fun getPermission() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(android.Manifest.permission.CAMERA), 101)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResult: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResult)
        if (grantResult[0] != PackageManager.PERMISSION_GRANTED) {
            getPermission()
        }
    }

    @SuppressLint("MissingPermission") // getPermission() already checks for permission, this just removes the error for "not" having perms
    fun open_camera() {
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

                }, null)
            }

            override fun onDisconnected(p0: CameraDevice) {}

            override fun onError(p0: CameraDevice, p1: Int) {}

        }, handler)
    }
}
>>>>>>> Stashed changes
