package com.github.zchu.example

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.activity_main.*
import java.lang.RuntimeException
import java.util.concurrent.TimeUnit
import kotlin.random.Random

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        stateful_view.onContentViewCreatedListener = {
            Toast.makeText(this, "onContentViewCreated", Toast.LENGTH_SHORT).show()

        }
        stateful_view.onErrorViewCreatedListener = {
            Toast.makeText(this, "onErrorViewCreated", Toast.LENGTH_SHORT).show()

        }
        stateful_view.onLoadingViewCreatedListener = {
            Toast.makeText(this, "onLoadingViewCreated", Toast.LENGTH_SHORT).show()

        }
        stateful_view.onRetryListener = {
            loadData()
        }

        loadData()
    }

    private fun loadData() {
        stateful_view.showLoading("加载中")
        Observable.just(Random(System.currentTimeMillis()).nextInt())
            .delay(4, TimeUnit.SECONDS)
            .flatMap {
                if (it % 2 == 0) {
                    Observable.just(it)
                } else {
                    Observable.error(RuntimeException("哇，出bug啦"))
                }
            }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<Int> {
                override fun onComplete() {
                }

                override fun onSubscribe(d: Disposable) {
                }

                override fun onNext(t: Int) {
                    stateful_view.showContent()
                }

                override fun onError(e: Throwable) {
                    stateful_view.showError(e.message)
                }

            })

    }
}
