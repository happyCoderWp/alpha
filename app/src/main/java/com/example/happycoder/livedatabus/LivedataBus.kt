package com.example.happycoder.livedatabus

/**
 * description : 基于liveData的消息总线，与RxBus相比生命周期安全，不用dispose消息监听
 * 传递
 * Created by Wangpeng09@kuaishou.com * on 2019-11-26
 */
object LivedataBus {
  private var mBuses = HashMap<String, BusMutableLiveData<Any>>()

  fun post(event: Any) {
    val key = event.javaClass.canonicalName ?: ""
    if (!mBuses.containsKey(key)) {
      mBuses[key] = BusMutableLiveData()
    }
    mBuses[key]?.value = event
  }

  fun <T> toObservable(eventType: Class<T>): BusMutableLiveData<T>? {
    val key = eventType.canonicalName ?: ""
    if (!mBuses.containsKey(key)) {
      mBuses[key] = BusMutableLiveData()
    }
    return mBuses[key] as BusMutableLiveData<T>
  }
}