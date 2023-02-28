package com.example.mediapipekotlin

import android.graphics.SurfaceTexture
import android.os.Bundle
import android.util.Log
import android.util.Size
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.mediapipekotlin.databinding.ActivityMainBinding
import com.google.mediapipe.components.CameraHelper.CameraFacing
import com.google.mediapipe.components.CameraXPreviewHelper
import com.google.mediapipe.components.ExternalTextureConverter
import com.google.mediapipe.components.FrameProcessor
import com.google.mediapipe.components.PermissionHelper
import com.google.mediapipe.formats.proto.LandmarkProto.NormalizedLandmarkList
import com.google.mediapipe.framework.AndroidAssetUtil
import com.google.mediapipe.framework.Packet
import com.google.mediapipe.framework.PacketGetter
import com.google.mediapipe.glutil.EglManager



class MainActivity : AppCompatActivity() {

    private val TAG = "MainActivity"
    private val INPUT_NUM_FACES_SIDE_PACKET_NAME = "num_faces"
    private val OUTPUT_LANDMARKS_STREAM_NAME = "multi_face_landmarks"
    private val BINARY_GRAPH_GPU_NAME = "face_mesh_mobile_gpu.binarypb"
    private val INPUT_VIDEO_STREAM_NAME = "input_video"
    private val OUTPUT_VIDEO_STREAM_NAME = "output_video"
    private val NUM_BUFFERS = 2
    private val NUM_FACES = 1
    private val FRONT_CAMERA = true
    private val FLIP_FRAMES_VERTICALLY = true
    private var p1xl:Float= 0f; private var p1yl:Float= 0f; private var p2xl:Float= 0f; private var p2yl:Float= 0f; private var p3xl:Float= 0f; private var p3yl:Float= 0f; private var p4xl:Float= 0f; private var p4yl:Float= 0f; private var p5xl:Float= 0f; private var p5yl:Float= 0f; private var p6xl:Float= 0f; private var p6yl:Float= 0f
    private var p1xr:Float= 0f; private var p1yr:Float= 0f; private var p2xr:Float= 0f; private var p2yr:Float= 0f; private var p3xr:Float= 0f; private var p3yr:Float= 0f; private var p4xr:Float= 0f; private var p4yr:Float= 0f; private var p5xr:Float= 0f; private var p5yr:Float= 0f; private var p6xr:Float= 0f; private var p6yr:Float= 0f
    private var boca1x:Float= 0f; private var boca2x:Float= 0f; private var boca3x:Float= 0f; private var boca4x:Float= 0f; private var boca5x:Float= 0f; private var boca6x:Float= 0f; private var boca1y:Float= 0f; private var boca2y:Float= 0f; private var boca3y:Float= 0f; private var boca4y:Float= 0f; private var boca5y:Float= 0f; private var boca6y:Float= 0f
    private var qtd_bocejo:Int = 0

    private var bocejo: TextView? = null
    private var olhos: TextView? = null
    private var sono: TextView? = null

    private var drowsinnessCounter:Int = 0
    private var retDrowsinnessEyes:Boolean = false
    private var retDrowsinnessMouth:Boolean = false

    //private var timer:Int = 0
    private var checkTimerBoca:Int = 0
    private var realTimerBoca:Int = 0
    private var retTimerBoca:Boolean = false
    private var retBocejo:Boolean = false

    private var checkTimerOlhos:Int = 0
    private var realTimerOlhos:Int = 0
    private var checkTimerOlhosSono:Int = 0
    private var retTimerOlhos:Boolean = false
    //private var retSono:Boolean = false

    companion object {
        private var alert: TextView? = null
        private var timer:Int = 0
        private var EAR:Double = 0.0
        private var ear_left:Double = 0.0
        private var ear_right:Double = 0.0
        private var timerThreshold:Int = 0
        private var threshold:Double = 0.0
        private var acess:Boolean = false

        init {
            System.loadLibrary("mediapipe_jni")
            System.loadLibrary("opencv_java3")
        }
    }

    protected var processor: FrameProcessor? = null
    protected var cameraHelper: CameraXPreviewHelper? = null
    private var previewFrameTexture: SurfaceTexture? = null
    private var previewDisplayView: SurfaceView? = null
    private var eglManager: EglManager? = null
    private var converter: ExternalTextureConverter? = null

    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(R.layout.activity_main)
        alert = findViewById(R.id.thresholdId)
        bocejo = findViewById(R.id.bocejoId)
        olhos = findViewById(R.id.sonoId)
        sono = findViewById(R.id.drowsinnessId)

        previewDisplayView = SurfaceView(this)
        setupPreviewDisplayView()
        AndroidAssetUtil.initializeNativeAssetManager(this)

        eglManager = EglManager(null)
        processor = FrameProcessor(
            this,
            eglManager!!.nativeContext,
            BINARY_GRAPH_GPU_NAME,
            INPUT_VIDEO_STREAM_NAME,
            OUTPUT_VIDEO_STREAM_NAME
        )
        processor!!
            .videoSurfaceOutput
            .setFlipY(
                FLIP_FRAMES_VERTICALLY
            )

        PermissionHelper.checkAndRequestCameraPermissions(this)
        val packetCreator = processor!!.packetCreator // null error
        val inputSidePackets: MutableMap<String, Packet> = HashMap()
        inputSidePackets[INPUT_NUM_FACES_SIDE_PACKET_NAME] = packetCreator.createInt32(NUM_FACES)
        processor!!.setInputSidePackets(inputSidePackets)

        if (true) {
            processor!!.addPacketCallback(
                OUTPUT_LANDMARKS_STREAM_NAME
            ) { packet: Packet ->
                Log.v(TAG, "Received multi face landmarks packet.")
                val multiFaceLandmarks =
                    PacketGetter.getProtoVector(
                        packet,
                        NormalizedLandmarkList.parser()
                    )
                //Log.e(TAG, "teste media")

                //Para o olho esquerdo :  [362, 385, 387, 263, 373, 380]
                //pontos faciais: https://github.com/google/mediapipe/blob/master/mediapipe/modules/face_geometry/data/canonical_face_model_uv_visualization.png
                //https://github.com/tensorflow/tfjs-models/commit/838611c02f51159afdd77469ce67f0e26b7bbb23
                p1xl = multiFaceLandmarks[0].landmarkList[362].x * 1920f
                p1yl = multiFaceLandmarks[0].landmarkList[362].y * 1920f

                p2xl = multiFaceLandmarks[0].landmarkList[385].x * 1920f
                p2yl = multiFaceLandmarks[0].landmarkList[385].y * 1920f

                p3xl = multiFaceLandmarks[0].landmarkList[387].x * 1920f
                p3yl = multiFaceLandmarks[0].landmarkList[387].y * 1920f

                p4xl = multiFaceLandmarks[0].landmarkList[263].x * 1920f
                p4yl = multiFaceLandmarks[0].landmarkList[263].y * 1920f

                p5xl = multiFaceLandmarks[0].landmarkList[373].x * 1920f
                p5yl = multiFaceLandmarks[0].landmarkList[373].y * 1920f

                p6xl = multiFaceLandmarks[0].landmarkList[380].x * 1920f
                p6yl = multiFaceLandmarks[0].landmarkList[380].y * 1920f

                //distanceEuclidian(float point1x, float point1y, float point2x, float point2y)

                //distanceEuclidian(float point1x, float point1y, float point2x, float point2y)
                val distance_left_eye_P6_P2 = distanceEuclidian(p2xl, p2yl, p6xl, p6yl)
                val distance_left_eye_P5_P3 = distanceEuclidian(p5xl, p5yl, p3xl, p3yl)
                val distance_left_eye_P1_P4 = distanceEuclidian(p4xl, p4yl, p1xl, p1yl)
                ear_left =
                    (distance_left_eye_P6_P2 + distance_left_eye_P5_P3) / (2.0 * distance_left_eye_P1_P4)

                //Para o olho direito :[33, 160, 158, 133, 153, 144]

                //Para o olho direito :[33, 160, 158, 133, 153, 144]
                p1xr = multiFaceLandmarks[0].landmarkList[33].x * 1920f
                p1yr = multiFaceLandmarks[0].landmarkList[33].y * 1920f

                p2xr = multiFaceLandmarks[0].landmarkList[160].x * 1920f
                p2yr = multiFaceLandmarks[0].landmarkList[160].y * 1920f

                p3xr = multiFaceLandmarks[0].landmarkList[158].x * 1920f
                p3yr = multiFaceLandmarks[0].landmarkList[158].y * 1920f

                p4xr = multiFaceLandmarks[0].landmarkList[133].x * 1920f
                p4yr = multiFaceLandmarks[0].landmarkList[133].y * 1920f

                p5xr = multiFaceLandmarks[0].landmarkList[153].x * 1920f
                p5yr = multiFaceLandmarks[0].landmarkList[153].y * 1920f

                p6xr = multiFaceLandmarks[0].landmarkList[144].x * 1920f
                p6yr = multiFaceLandmarks[0].landmarkList[144].y * 1920f

                val distance_right_eye_P6_P2 = distanceEuclidian(p2xr, p2yr, p6xr, p6yr)
                val distance_right_eye_P5_P3 = distanceEuclidian(p5xr, p5yr, p3xr, p3yr)
                val distance_right_eye_P1_P4 = distanceEuclidian(p4xr, p4yr, p1xr, p1yr)
                ear_right =
                    (distance_right_eye_P6_P2 + distance_right_eye_P5_P3) / (2.0 * distance_right_eye_P1_P4)

                EAR = (ear_right + ear_left) / 2.0

                boca1x = multiFaceLandmarks[0].landmarkList[308].x * 1920f
                boca1y = multiFaceLandmarks[0].landmarkList[308].y * 1920f

                boca2x = multiFaceLandmarks[0].landmarkList[87].x * 1920f
                boca2y = multiFaceLandmarks[0].landmarkList[87].y * 1920f

                boca3x = multiFaceLandmarks[0].landmarkList[312].x * 1920f
                boca3y = multiFaceLandmarks[0].landmarkList[312].y * 1920f

                boca4x = multiFaceLandmarks[0].landmarkList[62].x * 1920f
                boca4y = multiFaceLandmarks[0].landmarkList[62].y * 1920f

                boca5x = multiFaceLandmarks[0].landmarkList[317].x * 1920f
                boca5y = multiFaceLandmarks[0].landmarkList[317].y * 1920f

                boca6x = multiFaceLandmarks[0].landmarkList[82].x * 1920f
                boca6y = multiFaceLandmarks[0].landmarkList[82].y * 1920f

                //distanceEuclidian(float point1x, float point1y, float point2x, float point2y)

                //distanceEuclidian(float point1x, float point1y, float point2x, float point2y)
                val distance_labio_P6_P2 = distanceEuclidian(boca2x, boca2y, boca6x, boca6y)
                val distance_labio_P5_P3 = distanceEuclidian(boca5x, boca5y, boca3x, boca3y)
                val distance_labio_P1_P4 = distanceEuclidian(boca1x, boca1y, boca4x, boca4y)
                val ear_labio =
                    (distance_labio_P6_P2 + distance_labio_P5_P3) / (2.0 * distance_labio_P1_P4)


                if (acess) {
                    if (ear_right < threshold || ear_left < threshold) {
                        if (!retTimerOlhos) {
                            retTimerOlhos = true
                            checkTimerOlhos = timer
                        } else {
                            realTimerOlhos = timer - checkTimerOlhos
                        }
                        runOnUiThread { olhos?.text = "Olhos fechados" }
                    } else {
                        if (retTimerOlhos) {
                            retTimerOlhos = false
                            checkTimerOlhos = 0
                            realTimerOlhos = 0
                        }
                        runOnUiThread {
                            olhos?.text = "Olhos abertos"
                            retDrowsinnessEyes = false
                        }
                    }
                    if (realTimerOlhos == 4 && !retDrowsinnessEyes) {
                        Log.e("TAG", "Sonolencia: $EAR")
                        runOnUiThread {
                            olhos?.text = "Sonolencia acontecendo"
                            drowsinnessCounter++
                            sono?.text = "Sonolências: $drowsinnessCounter"
                            retDrowsinnessEyes = true
                        }
                        //mediaPlayer.start()
                    }
                    if (ear_labio > 0.05) { //boca aberta
                        if (retTimerBoca) { //segunda instancia, onde de fato cria o contador da duração do bocejo
                            realTimerBoca = timer - checkTimerBoca
                        } else { //primeira instancia do bocejo que cria um checkpoint do timer
                            checkTimerBoca = timer
                            checkTimerOlhosSono = timer
                            retTimerBoca = true
                        }
                    } else { //boca fechada
                        retTimerBoca = false
                        checkTimerBoca = 0
                        realTimerBoca = 0
                        retBocejo = true
                        retDrowsinnessMouth = false
                    }
                    if (realTimerBoca == 2 && retBocejo) {
                        Log.e("TAG", "BOCEJO: $ear_labio   $qtd_bocejo")
                        qtd_bocejo += 1
                        retBocejo = false
                        runOnUiThread { bocejo?.text = "Bocejos: $qtd_bocejo" }
                    }
                }
                Log.e(TAG, EAR.toString())
                Log.v(
                    TAG,
                    "[TS:"
                            + packet.timestamp
                            + "] "
                            + getMultiFaceLandmarksDebugString(multiFaceLandmarks)
                )
            }
        }

    }


    override fun onResume() {
        super.onResume()
        val timerThread =
            TimerThread()
        timerThread.start()

        val thresholdThread =
            ThresholdThread()
        thresholdThread.start()
        converter = ExternalTextureConverter(
            eglManager!!.context, NUM_BUFFERS
        )
        converter!!.setFlipY(FLIP_FRAMES_VERTICALLY)
        converter!!.setConsumer(processor)


        if (PermissionHelper.cameraPermissionsGranted(this)) {
            startCamera()
        }


    }

    override fun onPause() {
        super.onPause()
        converter!!.close()
        previewDisplayView!!.visibility = View.GONE
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        PermissionHelper.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }


    private fun onCameraStarted(surfaceTexture: SurfaceTexture) {
        previewFrameTexture = surfaceTexture
        previewDisplayView!!.visibility = View.VISIBLE
    }

    private fun cameraTargetResolution(): Size? {
        return null
    }

    private fun startCamera() {
        cameraHelper = CameraXPreviewHelper()
        cameraHelper!!.setOnCameraStartedListener { surfaceTexture: SurfaceTexture? ->
            onCameraStarted(surfaceTexture!!)
        }
        val cameraFacing = if (FRONT_CAMERA) CameraFacing.FRONT else CameraFacing.BACK
        cameraHelper!!.startCamera(
            this, cameraFacing, /*surfaceTexture=*/null, cameraTargetResolution()
        )
    }

    private fun computeViewSize(width: Int, height: Int): Size {
        return Size(width, height)
    }

    private fun onPreviewDisplaySurfaceChanged(
        width: Int, height: Int
    ) {
        val viewSize: Size = computeViewSize(width, height)
        val displaySize: Size = cameraHelper!!.computeDisplaySizeFromViewSize(viewSize)
        val isCameraRotated = cameraHelper!!.isCameraRotated

        converter!!.setSurfaceTextureAndAttachToGLContext(
            previewFrameTexture,
            if (isCameraRotated) displaySize.height else displaySize.width,
            if (isCameraRotated) displaySize.width else displaySize.height
        )
    }

    private fun setupPreviewDisplayView() {
        previewDisplayView!!.visibility = View.GONE
        val viewGroup = findViewById<ViewGroup>(R.id.preview_display_layout)
        viewGroup.addView(previewDisplayView)
        previewDisplayView!!
            .holder
            .addCallback(
                object : SurfaceHolder.Callback {
                    override fun surfaceCreated(holder: SurfaceHolder) {
                        processor!!.videoSurfaceOutput.setSurface(holder.surface)
                    }

                    override fun surfaceChanged(
                        holder: SurfaceHolder,
                        format: Int,
                        width: Int,
                        height: Int
                    ) {
                        onPreviewDisplaySurfaceChanged(width, height)
                    }

                    override fun surfaceDestroyed(holder: SurfaceHolder) {
                        processor!!.videoSurfaceOutput.setSurface(null)
                    }
                })
    }


    private fun getMultiFaceLandmarksDebugString(
        multiFaceLandmarks: List<NormalizedLandmarkList>
    ): String {
        if (multiFaceLandmarks.isEmpty()) {
            return "No face landmarks"
        }
        var multiFaceLandmarksStr =
            "Number of faces detected: ${multiFaceLandmarks.size} ".trimIndent()
        for ((faceIndex, landmarks) in multiFaceLandmarks.withIndex()) {
            multiFaceLandmarksStr += "#Face landmarks for face[$faceIndex]: ${landmarks.landmarkCount}"
            for ((landmarkIndex, landmark) in landmarks.landmarkList.withIndex()) {
                multiFaceLandmarksStr += "Landmark [$landmarkIndex]: (${landmark.x}, ${landmark.y}, ${landmark.z})"
            }
        }
        return multiFaceLandmarksStr
    }
    private fun distanceEuclidian(
        point1x: Float,
        point1y: Float,
        point2x: Float,
        point2y: Float
    ): Double {
        return positiveNumber(kotlin.math.sqrt(((point2x - point1x) * (point2x - point1x) + (point2y - point1y) * (point2y - point1y)).toDouble()))
    }
    private fun positiveNumber(number: Double): Double {
        return if (number < 0) {
            number * -1
        } else number
    }
    class TimerThread : Thread() {
        override fun run() {
            while (true) {
                try {
                    sleep(1000)
                    timer++
                    //Log.e("TAG", "timerBoca: $timer")
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
            }
        }
    }

    class ThresholdThread : Thread() {
        override fun run() {
            val listEARs = ArrayList<Double>()
            alert?.setOnClickListener {
                while (true) {
                        try {
                            sleep(100)
                            Log.e("TAG", "teste thread: ")
                            if (timerThreshold <= 70) {
                                if (ear_right >= 0.12 && ear_left >= 0.12) { //valor a ser revisado
                                    listEARs.add(ear_left)
                                    listEARs.add(ear_right)
                                    Log.e("TAG", "Teste EAR Esq: $ear_left")
                                    Log.e("TAG", "Teste EAR Dir: $ear_right")
                                    timerThreshold++
                                } else {
                                    Log.e("TAG", "piscada")
                                }
                            } else {
                                var soma = 0.0
                                for (n in listEARs) {
                                    soma += n
                                }
                                val media = soma / listEARs.size
                                threshold = 0.08 * media / 0.16
                                Log.e("TAG", "Media EAR $media")
                                Log.e("TAG", "Threshold $threshold")
                                currentThread().interrupt()
                                acess = true
                                alert?.setVisibility(View.GONE)
                                break
                            }
                        } catch (e: InterruptedException) {
                            e.printStackTrace()
                        }
                    }

            }
        }
    }
}