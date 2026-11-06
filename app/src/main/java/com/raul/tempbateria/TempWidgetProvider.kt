package com.raul.tempbateria

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Bundle
import android.widget.RemoteViews

class TempWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) {
        for (id in ids) actualizar(context, manager, id)
    }

    override fun onAppWidgetOptionsChanged(
        context: Context,
        manager: AppWidgetManager,
        id: Int,
        opciones: Bundle
    ) {
        actualizar(context, manager, id)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        // Al tocar el widget se vuelve a leer la temperatura
        if (intent.action == ACCION_REFRESCAR) {
            val manager = AppWidgetManager.getInstance(context)
            val ids = manager.getAppWidgetIds(ComponentName(context, TempWidgetProvider::class.java))
            for (id in ids) actualizar(context, manager, id)
        }
    }

    companion object {

        const val ACCION_REFRESCAR = "com.raul.tempbateria.REFRESCAR"

        /** Temperatura de la batería en °C. No necesita permisos especiales. */
        fun leerTemperatura(context: Context): Double {
            val intent = context.registerReceiver(
                null,
                IntentFilter(Intent.ACTION_BATTERY_CHANGED)
            )
            val raw = intent?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) ?: 0
            return raw / 10.0   // Android la da en décimas de grado
        }

        private fun actualizar(context: Context, manager: AppWidgetManager, id: Int) {
            val densidad = context.resources.displayMetrics.density
            val opciones = manager.getAppWidgetOptions(id)
            val anchoDp = opciones.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH, 110)
            val altoDp = opciones.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT, 50)

            // Límites de tamaño para que RemoteViews no rechace el bitmap
            val ancho = (anchoDp * densidad).toInt().coerceIn(120, 600)
            val alto = (altoDp * densidad).toInt().coerceIn(60, 300)

            val bitmap = TempRenderer.dibujar(leerTemperatura(context), ancho, alto)

            val vistas = RemoteViews(context.packageName, R.layout.widget_temp)
            vistas.setImageViewBitmap(R.id.imagen, bitmap)

            val refresco = PendingIntent.getBroadcast(
                context,
                0,
                Intent(context, TempWidgetProvider::class.java).setAction(ACCION_REFRESCAR),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            vistas.setOnClickPendingIntent(R.id.imagen, refresco)

            manager.updateAppWidget(id, vistas)
        }
    }
}
