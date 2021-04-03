package com.rouninlabs.another_brother.method

import android.content.Context
import android.graphics.BitmapFactory
import android.util.Log
import com.brother.ptouch.sdk.Printer
import com.rouninlabs.another_brother.BrotherManager
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import kotlinx.coroutines.*

/**
 * Command for printing an image to a Brother printer.
 * This support both one-time as well as the standard openCommunication/print/closeCommunication
 * approach.
 */
class PrintImageMethodCall(val context: Context, val call: MethodCall, val result: MethodChannel.Result) {
    companion object {
        const val METHOD_NAME = "printImage"
    }

    fun execute() {

        GlobalScope.launch(Dispatchers.IO) {

            // TODO Run on background thread.
            val dartPrintInfo: HashMap<String, Any> = call.argument<HashMap<String, Any>>("printInfo")!!
            val imageBytes: ByteArray = call.argument<ByteArray>("imageBytes")!!
            val printerId: String = call.argument<String>("printerId")!!

            Log.e("Brother", "Image Bytes: $imageBytes - Size ${imageBytes.size}")
            // Bytes to bitmap
            val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
            // Decoded Printer Info
            val printInfo = printerInfofromMap(dartPrintInfo)
            Log.e("Brother", "PrintInfo: $dartPrintInfo")
            Log.e("Brother", "Parsed Info: ${printInfo}")

            // A print request is considered one-time if there was no printer tracked with this ID.
            // this will open a new connection and close it when done.
            // If it is not one-time it means someone must have already opened a connection using
            // the startCommunication() API. When endCommunication() is called that printer will be removed.
            // Create Printer
            val trackedPrinter = BrotherManager.getPrinter(printerId = printerId)
            val isOneTime:Boolean = trackedPrinter == null;
            val printer = trackedPrinter?: Printer()

            // Prepare local connection.
            setupConnectionManagers(context = context, printer = printer, printInfo = printInfo)

            // Set Printer Info
            printer.printerInfo = printInfo
            val specs = printer.printerSpec

            // Start communication
            if (isOneTime) {
                // Note: Starting a communication does not seem to impact whether we can print or
                // not. Calling print without calling this seems to still print fine.
                val started: Boolean = printer.startCommunication()
            }

            // Print Image
            val printResult = printer.printImage(bitmap)

            // End Communication
            if (isOneTime) {
                val connectionClosed: Boolean = printer.endCommunication()
            }
            // Recycle bitmap
            if (!bitmap.isRecycled) {
                bitmap.recycle()
            }

            // Encode PrinterStatus
            val dartPrintStatus = printResult.toMap()
           withContext(Dispatchers.Main) {
               // Set result Printer status.
               result.success(dartPrintStatus)
               //result.error("Error", "Method not implemented", "")
           }
        }

    }
}