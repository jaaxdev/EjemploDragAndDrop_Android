package com.jaax.select

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipDescription
import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.*
import android.widget.*

class MainActivity : AppCompatActivity(), View.OnLongClickListener {
    private lateinit var parentLayout: RelativeLayout
    private lateinit var trash: ImageView
    private lateinit var btnsAddTxt: Button
    private lateinit var btnsAddImgview: Button
    private lateinit var toast: Toast
    private lateinit var layoutParams: RelativeLayout.LayoutParams
    private var inicialEventoX = 0F
    private var inicialEventoY = 0F
    private var inicialLayoutX = 0
    private var inicialLayoutY = 0

    @SuppressLint("ShowToast")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        parentLayout = findViewById(R.id.workspace)
        trash = findViewById(R.id.trashbin)
        btnsAddTxt = findViewById(R.id.add_txt)
        btnsAddImgview = findViewById(R.id.add_img)
        toast = Toast.makeText(this, "Toast", Toast.LENGTH_SHORT)

        //evento de arraste, sólo sirve en este caso para eliminar los view que se pongan sobre la imagen de basura
        trash.setOnDragListener( addDropListener() )
        parentLayout.setOnDragListener( onDragListener )
    }

    override fun onResume() {
        super.onResume()
        //crear nuevo TxtView
        btnsAddTxt.setOnClickListener {
            addTxtParentLayout(parentLayout)
        }

        //crear nuevo ImageView
        btnsAddImgview.setOnClickListener {
            addImgParentLayout(parentLayout)
        }
    }

    private fun addDropListener(): View.OnDragListener {
        return View.OnDragListener { _, event ->
            when( event.action ) {
                DragEvent.ACTION_DROP -> {
                    val v = event.localState as View
                    parentLayout.removeView(v)
                    toast.setText("View eliminado")
                    toast.setGravity(Gravity.BOTTOM, 0, 0)
                    toast.show()
                }
            }
            true
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun addTxtParentLayout(parent: ViewGroup ) {
        val txtview = TextView( this@MainActivity )
        txtview.setText("Texto nuevo")
        txtview.x = 100F
        txtview.y = 100F
        txtview.textSize = 25F
        txtview.setTextColor(Color.MAGENTA)

        parent.addView(txtview)
        txtview.setOnTouchListener( onTouchListener ) //toque simple, si re-posiciona pero no puede eliminarse
        //txtview.setOnLongClickListener(this)
        // TODO -> La otra opcion es eliminar por coordenadas, que en cierto X, Y sea el área de borrado
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun addImgParentLayout(parent: ViewGroup ) {
        val imgview = ImageView( this@MainActivity )
        //coordenadas X, Y
        imgview.x = 0F
        imgview.y = 100F
        imgview.setImageResource(R.drawable.andy_src_imgview)
        imgview.scaleX = 5F
        imgview.scaleY = 5F

        parent.addView(imgview)

        imgview.setOnLongClickListener( this ) //arrastra y elimina pero no re-posiciona
        //imgview.setOnDragListener( onDragListener )
        //imgview.setOnTouchListener( onTouchListener )
    }


    private val onDragListener = View.OnDragListener { view, dragEvent ->
        when( dragEvent.action ) {
            DragEvent.ACTION_DRAG_STARTED -> {
                dragEvent.clipDescription.hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN)
            }
            DragEvent.ACTION_DRAG_ENTERED -> {
                view.invalidate()
            }
            DragEvent.ACTION_DRAG_LOCATION -> {
            }
            DragEvent.ACTION_DRAG_EXITED -> {
                view.invalidate()
            }
            DragEvent.ACTION_DROP -> {
                view.invalidate()
                val tmpView = dragEvent.localState as View
                val parentView = tmpView.parent as ViewGroup
                parentView.removeView(tmpView)
                parentLayout.addView(tmpView)
                tmpView.visibility = View.VISIBLE
            }

            DragEvent.ACTION_DRAG_ENDED -> {
                view.invalidate()
            }
        }
        true
    }


    //evento de toque simple, sólo arrastra y sitúa con nuevas coordenadas
    private val onTouchListener = View.OnTouchListener { view, event ->
        when( event?.action ) {
            MotionEvent.ACTION_DOWN -> {
                layoutParams = view?.layoutParams as RelativeLayout.LayoutParams
                inicialLayoutX = layoutParams.leftMargin
                inicialLayoutY = layoutParams.topMargin
                inicialEventoX = event.rawX
                inicialEventoY = event.rawY
                view.performClick()
            }

            MotionEvent.ACTION_MOVE -> {
                layoutParams = view?.layoutParams as RelativeLayout.LayoutParams
                layoutParams.leftMargin  = (inicialLayoutX + event.rawX - inicialEventoX).toInt()
                layoutParams.topMargin = (inicialLayoutY + event.rawY - inicialEventoY).toInt()
                view.layoutParams = layoutParams
            }
        }
        true
    }

    override fun onLongClick(it: View?): Boolean {
        val item = ClipData.Item(it?.tag.toString())
        val mime = arrayOf(ClipDescription.MIMETYPE_TEXT_PLAIN)
        val data = ClipData(it?.tag.toString(), mime, item)
        val shadowBuilder = View.DragShadowBuilder(it)

        //por si la versión de ANDROID es menor a 7 (Nougat)
        //se puede usar sólo startDrag()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            it?.startDragAndDrop( data, shadowBuilder, it, 0 )
        } else {
            it?.startDrag( data, shadowBuilder, it, 0 )
        }
        return true
    }
}