package com.example.happycoder.livedatabus

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import org.jetbrains.annotations.NotNull


/**
 * description : 自定义LiveData实现类，重写父类的observer、remove方法
 * Created by Wangpeng09@kuaishou.com * on 2019-11-26
 */
class BusMutableLiveData<T> : MutableLiveData<T>() {

  private val FIELD_OBSERVERS = "mObservers"
  private val FIELD_LAST_VERSION = "mLastVersion"
  private val FIELD_VERSION = "mVersion"
  private val METHOD_MAP_GET = "get"
  private var mObserverMap = HashMap<Observer<T>, Observer<T>>()

  override fun observe(owner: LifecycleOwner, observer: Observer<in T>) {
    super.observe(owner, observer)
    try {
      hook(observer)
    } catch (e: Exception) {
      e.printStackTrace()
      // TODO 打点上报
    }
  }

  override fun observeForever(observer: Observer<in T>) {
    if (!mObserverMap.containsKey(observer)) {
      mObserverMap[observer as Observer<T>] = ObserverWrapper(observer)
    }
    super.observeForever(observer)
  }

  override fun removeObserver(observer: Observer<in T>) {
    super.removeObserver(if (mObserverMap.containsKey(observer)) {
      mObserverMap.remove(observer) as Observer<in T>
    } else {
      observer
    })
  }

  /**
   * 对于一个liveData，每个新订阅者其version都是-1，这样就会导致LiveDataBus注册一个新订阅者后，
   * 当LifeCircleOwner的状态发生变化的时候，这个订阅者立刻会收到一个回调，即使这个设置的动作发生在订阅之前。
   * 故采用如下逻辑处理：当LiveDataBus注册一个新订阅者时，修改其version值为所监听liveData的mVersion。
   */
  private fun hook(@NotNull observer: Observer<in T>) {
    // 1 反射调用当前LiveData对象的mObservers变量（类型为SafeIterableMap<Observer<? super T>,ObserverWrapper>）
    //   取得Map中以形参observer为键的值objectWrapper
    val fieldObservers = LiveData::class.java.getDeclaredField(FIELD_OBSERVERS)
    fieldObservers.isAccessible = true
    val objectObservers = fieldObservers.get(this)
    val classObservers = objectObservers.javaClass
    val methodGet = classObservers.getDeclaredMethod(METHOD_MAP_GET, Any::class.java)
    methodGet.isAccessible = true
    val objectWrapperEntry = methodGet.invoke(objectObservers, observer)
    var objectWrapper: Any? = null
    if (objectWrapperEntry is Map.Entry<*, *>) {
      objectWrapper = objectWrapperEntry.value
    }
    // 2 取得当前LiveData所对应的version值objectVersion
    val fieldVersion = LiveData::class.java.getDeclaredField(FIELD_VERSION)
    fieldVersion.isAccessible = true
    val objectVersion = fieldVersion.get(this)
    // 3 将objectVersion赋值给objectWrapper的mLastVersion
    val classObserverWrapper = objectWrapper?.javaClass?.superclass
    val fieldLastVersion = classObserverWrapper?.getDeclaredField(FIELD_LAST_VERSION)
    fieldLastVersion?.isAccessible = true
    fieldLastVersion?.set(objectWrapper, objectVersion)
  }
}