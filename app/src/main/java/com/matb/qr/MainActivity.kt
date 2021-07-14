package com.matb.qr

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.util.Size
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.barcode.Barcode
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import com.matb.qr.databinding.ActivityMainBinding
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


// TODO: add logic to prompt user to center QR in box
@ExperimentalGetImage
class MainActivity :
    AppCompatActivity(),
    ActivityCompat.OnRequestPermissionsResultCallback,
    QRScannedDialogFragment.QRScannedDialogListener
{
    private lateinit var binding: ActivityMainBinding
    private lateinit var cameraExecutor: ExecutorService

    private var cameraProvider: ProcessCameraProvider? = null

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (allPermissionsGranted())
        {
            startCamera()
        }
        else
        {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }

        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    private fun startCamera()
    {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            cameraProvider = cameraProviderFuture.get()
            bindCameraUseCases()
        }, ContextCompat.getMainExecutor(this))

    }

    private fun bindCameraUseCases()
    {
        if (cameraProvider != null)
        {
            val scanner = BarcodeScanning.getClient(SCANNER_OPTIONS)

            val preview = Preview.Builder()
                .build()
                .also { it.setSurfaceProvider(binding.viewFinder.surfaceProvider) }

            val imageAnalyzer = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .setTargetResolution(CAMERA_RESOLUTION)
                .build()
                .also { it.setAnalyzer(cameraExecutor,
                    { imageProxy ->
                        val image = imageProxy.image
                        if (image != null)
                        {
                            scanner.process(InputImage.fromMediaImage(image, imageProxy.imageInfo.rotationDegrees))
                                .addOnSuccessListener { result -> onQRCodeProcessed(result, scanner) }
                                .addOnFailureListener { e: Exception -> Log.d(LOG_TAG, "Failed to process QR image " + e.localizedMessage) }
                                .addOnCompleteListener {
                                    image.close()
                                    imageProxy.close()
                                }
                        }
                    })
                }

            try
            {
                cameraProvider!!.unbindAll()
                cameraProvider!!.bindToLifecycle(this,
                    CameraSelector.DEFAULT_BACK_CAMERA, preview, imageAnalyzer)
            }
            catch (exc: Exception)
            {
                Log.e(LOG_TAG, "Use case binding failed", exc)
            }

        }
    }

    /**
     * Stop the camera and display QR value in a dialog
     */
    private fun onQRCodeProcessed(codes: List<Barcode>, scanner: BarcodeScanner)
    {
        if (codes.isEmpty())
        {
            Log.d(LOG_TAG, "No barcode has been detected")
        }

        val qrCode = codes.firstOrNull()
        if (qrCode != null)
        {
            cameraProvider!!.unbindAll()
            scanner.close()

            val bundle = Bundle()
            bundle.putString(QRScannedDialogFragment.CODE_VALUE_KEY, qrCode.rawValue)

            val dialog = QRScannedDialogFragment()
            dialog.arguments = bundle
            dialog.show(supportFragmentManager, "QRScannedDialogFragment")
        }
    }

    /**
     * Restart camera scanning
     */
    override fun onDismissDialog()
    {
        bindCameraUseCases()
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    override fun onDestroy()
    {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>,
                                            grantResults: IntArray)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS)
        {
            if (allPermissionsGranted())
            {
                startCamera()
            }
            else
            {
                Toast.makeText(this,
                    "Permissions not granted by the user.",
                    Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    companion object
    {
        private const val LOG_TAG = "CameraXBasic"
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
        private val CAMERA_RESOLUTION: Size = Size(640, 480)
        private val SCANNER_OPTIONS = BarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_QR_CODE, Barcode.FORMAT_AZTEC)
            .build()
    }
}