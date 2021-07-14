package com.matb.qr

import android.util.Log
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.Barcode
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage

class QRCodeAnalyzer : ImageAnalysis.Analyzer
{
    private val scannerOptions = BarcodeScannerOptions.Builder()
        .setBarcodeFormats(Barcode.FORMAT_QR_CODE, Barcode.FORMAT_AZTEC)
        .build()

    private val scanner: BarcodeScanner = BarcodeScanning.getClient(scannerOptions)

    @ExperimentalGetImage
    override fun analyze(imageProxy: ImageProxy)
    {
        val image = imageProxy.image
        if (image != null)
        {
            scanner.process(InputImage.fromMediaImage(image, imageProxy.imageInfo.rotationDegrees))
                .addOnSuccessListener { result -> onSuccess(result) }
                .addOnFailureListener { e: Exception -> Log.d(TAG, "Failed to process QR image " + e.localizedMessage) }
                .addOnCompleteListener {
                    image.close()
                    imageProxy.close()
                }
        }
    }

    private fun onSuccess(codes: List<Barcode>)
    {
        if (codes.isEmpty())
        {
            Log.v(TAG, "No barcode has been detected")
        }

        for (code in codes)
        {
            logQRCodeInfo(code)
        }
    }

    private fun logQRCodeInfo(code: Barcode?)
    {
        if (code != null)
        {
            Log.i(TAG, "Raw value: " + code.rawValue!!)
            Log.i(TAG, "Bounding box: " + code.boundingBox!!.flattenToString())
        }
    }

    companion object {
        private const val TAG = "QRCodeAnalyzer"
    }
}