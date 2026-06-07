package de.lemke.oneuisample

import android.content.Context
import leakcanary.LeakCanary

fun openLeakCanary(context: Context) = context.startActivity(LeakCanary.newLeakDisplayActivityIntent())
