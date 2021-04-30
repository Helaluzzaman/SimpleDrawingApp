package com.hbsoft.simpledrawingapp
import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.media.MediaScannerConnection
import android.os.AsyncTask
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.get
import com.google.android.material.snackbar.Snackbar
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity() {
    lateinit var drawingView: DrawingView
    lateinit var ib_undo: ImageButton
    lateinit var ib_redo: ImageButton
    lateinit var ib_brush : ImageButton
    lateinit var ll_colorPicker: LinearLayout
    lateinit var b_colorPicker_current: Button
    lateinit var ib_save: ImageButton
    lateinit var fl_container: FrameLayout
    var selectedBrushsize = 1
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        drawingView = findViewById(R.id.dv_myCanvas)
        ib_undo = findViewById(R.id.ib_undo)
        ib_undo.setOnClickListener { drawingView.undoOnClick() }
        ib_undo.setOnLongClickListener { drawingView.undoAllonClick() }
        ib_redo = findViewById(R.id.ib_redo)
        ib_redo.setOnClickListener{ drawingView.redoOnClick()}

        ib_brush = findViewById(R.id.ib_brush)
        ib_brush.setOnClickListener {
            showBrushDialog(selectedBrushsize)
        }
        ll_colorPicker = findViewById(R.id.ll_color)
        b_colorPicker_current = ll_colorPicker[7] as Button
        b_colorPicker_current.isSelected = true

        fl_container = findViewById(R.id.fl_canvas_container)
        ib_save = findViewById(R.id.ib_save)
        ib_save.setOnClickListener{
            val bitmap = viewToBitmap(fl_container)
            SavingAsynctask(it, bitmap).execute()

        }

    }
    fun pickSetColor(view: View){
        val b_color_picker = view as Button
        if(b_color_picker != b_colorPicker_current){
            b_color_picker.isSelected = true
            b_colorPicker_current.isSelected = false
            val newColor = b_color_picker.tag.toString()
            drawingView.setColor(newColor)
            b_colorPicker_current = b_color_picker

        }
    }

    fun showBrushDialog(sBS: Int){
        val brushDialog  = Dialog(this)
        brushDialog.setContentView(R.layout.brush_dialog)
        val vsmall = brushDialog.findViewById<ImageButton>(R.id.ib_very_small)
        val small = brushDialog.findViewById<ImageButton>(R.id.ib_small)
        val medium = brushDialog.findViewById<ImageButton>(R.id.ib_medium)
        val large = brushDialog.findViewById<ImageButton>(R.id.ib_large)
        when(sBS){
            0 -> vsmall.isSelected = true
            1 -> small.isSelected = true
            2 -> medium.isSelected = true
            3 -> large.isSelected = true
        }
        brushDialog.show()
        vsmall.setOnClickListener{
            drawingView.setBrushSize(4F)
            selectedBrushsize = 0
            brushDialog.dismiss()
        }
        small.setOnClickListener{
            drawingView.setBrushSize(8F)
            selectedBrushsize = 1
            brushDialog.dismiss()
        }
        medium.setOnClickListener{
            drawingView.setBrushSize(12F)
            selectedBrushsize = 2
            brushDialog.dismiss()
        }
        large.setOnClickListener{
            drawingView.setBrushSize(16F)
            selectedBrushsize = 3
            brushDialog.dismiss()
        }

    }

    fun requestReadPermission(){
        val readpermission = Manifest.permission.READ_EXTERNAL_STORAGE
        if(ActivityCompat.shouldShowRequestPermissionRationale(this, readpermission)){
            Toast.makeText(this, "storage read permission need to load " +
                    "image from gallery.",Toast.LENGTH_SHORT).show()
            ActivityCompat.requestPermissions(this, arrayOf(readpermission), READ_STOREAGE_REQUEST_CODE)
        }else{
            ActivityCompat.requestPermissions(this, arrayOf(readpermission), READ_STOREAGE_REQUEST_CODE)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
    fun checkStoragePermission(): Boolean{
        return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
    }

    companion object {
        const val READ_STOREAGE_REQUEST_CODE = 1
        const val IMAGE_REQUEST = 2
    }

    fun pickImage(view: View) {
        if(checkStoragePermission()){
            val pickIntent = Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI)
            startActivityForResult(pickIntent, IMAGE_REQUEST)
        }else{
            requestReadPermission()
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == IMAGE_REQUEST){
            if(resultCode == Activity.RESULT_OK){
                val background = findViewById<ImageView>(R.id.iv_background)
                if(data?.data != null){
                    background.setImageURI(data.data)
                }else{
                    Toast.makeText(this, "something went wrong, try again.", Toast.LENGTH_SHORT).show()
                }
            }else{
                Toast.makeText(this, "You have not pick image.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun savingFile(mBitmap: Bitmap): String{
        var result = ""
        try{
            val bytes = ByteArrayOutputStream()
            mBitmap.compress(Bitmap.CompressFormat.PNG, 95, bytes)  // writing in the byte
            val file = File(externalCacheDir!!.absoluteFile.toString() + File.separator +
                    "simpleDrawing_" + System.currentTimeMillis()/1000 + ".PNG")
            val fos = FileOutputStream(file)
            fos.write(bytes.toByteArray())
            fos.close()
            result = file.absolutePath

        }catch (e: Exception){
            Toast.makeText(this, "some thing went wrong", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
            result = "Failed"
        }
        return result
    }
    private inner class SavingAsynctask(val view: View, val mBitmap: Bitmap) : AsyncTask<Any, Unit, String>(){
        override fun doInBackground(vararg params: Any?): String {
            return savingFile(mBitmap)
        }

        override fun onPostExecute(result: String?) {
            val snb = Snackbar.make(view, result.toString(), Snackbar.LENGTH_SHORT)
            snb.show()
            MediaScannerConnection.scanFile(this@MainActivity,
            arrayOf(result), null){
                path, uri -> val shareIntent = Intent()
                shareIntent.action = Intent.ACTION_SEND
                shareIntent.putExtra(Intent.EXTRA_STREAM, uri)
                shareIntent.type = "image/png"
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                    startActivity(Intent.createChooser(shareIntent, "Share it", null))
                }
            }
        }

    }

    fun viewToBitmap(view: View): Bitmap{
        val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        if(view.background != null){
            view.background.draw(canvas)
        }else{
            canvas.drawColor(Color.WHITE)
        }
        view.draw(canvas)
        return bitmap
    }

}