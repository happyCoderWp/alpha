package com.example.happycoder.livedatabus

import androidx.annotation.Nullable
import androidx.lifecycle.Observer

/**
 * description :对非生命周期感知的observerForever引起的onChange 做拦截，
 * 来解决使用observerForever新加一个observer时引起的无意义onChange回调（wrapper.activeStateChanged(true)触发）
 * Created by Wangpeng09@kuaishou.com * on 2019-11-26
 */
open class ObserverWrapper<T>(private val observer: Observer<in T>) : Observer<T> {

  private val CLASS_LIVEDATA = "android.arch.lifecycle.LiveData"
  private val METHOD_LIVEDATA_OBSERVER_FOREVER = "observeForever"

  private val isCallOnObserve: Boolean
    get() {
      val stackTrace = Thread.currentThread().stackTrace
      if (stackTrace.isNotEmpty()) {
        for (element in stackTrace) {
          if (CLASS_LIVEDATA == element.className && METHOD_LIVEDATA_OBSERVER_FOREVER == element.methodName) {
            return true
          }
        }
      }
      return false
    }

  override fun onChanged(@Nullable t: T) {
    if (isCallOnObserve) {
      return
    }
    observer.onChanged(t)
  }
}