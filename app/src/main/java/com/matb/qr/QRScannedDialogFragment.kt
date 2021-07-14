package com.matb.qr

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment

class QRScannedDialogFragment : DialogFragment()
{
    private lateinit var listener: QRScannedDialogListener

    interface QRScannedDialogListener
    {
        fun onDismissDialog()
    }

    override fun onAttach(context: Context)
    {
        super.onAttach(context)
        try
        {
            // Instantiate the QRScannedDialogListener so we can send events to the host
            listener = context as QRScannedDialogListener
        }
        catch (e: ClassCastException)
        {
            // The activity doesn't implement the interface, throw exception
            throw ClassCastException(("$context must implement QRScannedDialogListener"))
        }
    }

    override fun onCreateDialog(bundle: Bundle?): Dialog
    {
        return activity?.let {
            val message = getString(R.string.qr_scanned_dialog_message) + "\n" +
                    requireArguments().getString(CODE_VALUE_KEY)
            AlertDialog.Builder(it).setMessage(message).create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    override fun onDismiss(dialog: DialogInterface)
    {
        super.onDismiss(dialog)
        listener.onDismissDialog()
    }

    companion object
    {
        const val CODE_VALUE_KEY = "codeValue"
    }
}