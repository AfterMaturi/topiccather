package com.example.topiccatcher_android

import android.annotation.SuppressLint
import android.content.Context.AUDIO_SERVICE
import android.content.Intent
import android.graphics.Color
import android.media.AudioManager
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import coil.imageLoader
import coil.load
import coil.memory.MemoryCache
import coil.request.ImageRequest
import com.example.topiccatcher_android.databinding.FragmentMeetingRoomBinding

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.http.GET
import java.util.*
import java.util.concurrent.TimeUnit


class MeetingRoomFragment : Fragment() {

    private var speechRecognizer : SpeechRecognizer? = null // 音声取得・認識用
    private var resultList = mutableListOf("")  // 過去の認識結果を保持しておくリスト
    private var speechRecognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
    private lateinit var mAudioManager : AudioManager
    private var _binding : FragmentMeetingRoomBinding? = null
    private val binding get() = _binding!!
    private var resultText : String = ""
//    private var kentiRequestStatus : Int = 0
    var client = OkHttpClient.Builder()
        .connectTimeout(60*15, TimeUnit.SECONDS)
        .writeTimeout(60*15, TimeUnit.SECONDS)
        .readTimeout(60*15, TimeUnit.SECONDS)
        .build()
    var url_kenti = "https://asia-northeast1-topic-catcher.cloudfunctions.net/kenti2/"
    val retrofit = Retrofit.Builder()
        .baseUrl(url_kenti)
        .client(client)
        .build()
    val url_wordcloud = "https://asia-northeast1-topic-catcher.cloudfunctions.net/wordcloud"

    // サービスクラスの実装オブジェクト取得
    val service = retrofit.create(MyService::class.java)
    val get = service.getRawResponseForPosts()

    val TAG = "main-callback"     // ログ出力名


    val hnd0 = Handler()
    val rnb0 = object : Runnable{
        override fun run() {
            val imageLoader = binding.wordCloudImage.context.imageLoader
            val request = ImageRequest.Builder(binding.wordCloudImage.context)
                .data(url_wordcloud)
                .target(binding.wordCloudImage)
                .build()
            Log.d("wordcloud", "ワードクラウドURL呼び出し")
            imageLoader.memoryCache?.remove(MemoryCache.Key(url_wordcloud))// キャッシュクリア
            imageLoader.enqueue(request)   // 画面に表示

            // アルファ値を1.0fから0.0fへ変化させるフェードアウトアニメーション
            val fadeAnim = AlphaAnimation(1.0f, 0.0f)
            // 5秒間(5000ミリ秒)かけて行う
            fadeAnim.duration = 5000
            // アルファ値をアニメーション終了後の値を維持する
            fadeAnim.fillAfter = true

/*
            // Retrofit本体
            val retrofit = Retrofit.Builder().apply {
                // 呼び出したいURLを指定(URLの最後は / で終わらないとクラッシュする)←なんで？？？
                baseUrl(url_kenti)
            }.build()
 */
            // 連続で呼び出す場合はget.clone.enqueueらしい, get.enqueueだと2回目にクラッシュした
            // enqueueだと非同期通信
            // executeだと同期通信,ただしAndroidアプリだとメインスレッドで同期通信できないみたいなので、スレッドを変えないといけない（やり方わからん）
            // enqueueにはコールバック関数（？）を設定する（以下は参考サイトまま）

//            if(kentiRequestStatus == 0) {
//                kentiRequestStatus = 1
                Log.d("kenti", "検知URL呼び出し：" + Date().toString())
                get.clone().enqueue(object : Callback<ResponseBody> {
                    @SuppressLint("SetTextI18n")
                    override fun onResponse(
                        call: Call<ResponseBody>,
                        response: Response<ResponseBody>
                    ) {
                        if (response.isSuccessful) {
                            response.body()?.string()?.let { json ->
                                Log.d(TAG + "-if", json)       // 検知信号を取得するとここでログが出た
                                //取得した内容から表示するテキストを分ける操作
                                val status_code = json
                                if (status_code == "1") {
                                    binding.textTopicStatus.setText("トピックの切り替わりを検知しました")
                                    binding.textTopicStatus.setBackgroundResource(R.drawable.kenti_signal_bar)
                                } else if (status_code == "0") {
                                    binding.textTopicStatus.setText("トピックの変化はありません")
                                    binding.textTopicStatus.setBackgroundResource(R.drawable.kenti_signal_bar_default)
                                } else {
                                    binding.textTopicStatus.setText("error:想定外の検知信号")//一応の例外操作
                                    binding.textTopicStatus.setBackgroundResource(R.drawable.kenti_signal_bar)
                                }
                            }
                        } else {
                            val msg = "HTTP error. HTTP status code: ${response.code()}"
                            Log.e(TAG + "onResponse", msg)     // 通信失敗だとこっちが実行？
                            binding.textTopicStatus.setText("HTTP error: ${response.code()}")//一応の例外操作
                            binding.textTopicStatus.setBackgroundResource(R.drawable.kenti_signal_bar)
                        }
//                        kentiRequestStatus = 0
                        Log.d("kenti", "検知URL呼び出し完了：正常終了：" + Date().toString())
                    }

                    override fun onFailure(
                        call: Call<ResponseBody>,
                        t: Throwable
                    ) {
                        Log.e(TAG + "onFailure", t.toString())
                        binding.textTopicStatus.setText("RunTime Error")//一応の例外操作
                        binding.textTopicStatus.setBackgroundResource(R.drawable.kenti_signal_bar)
//                        kentiRequestStatus = 0
                        Log.d("kenti", "検知URL呼び出し完了：異常終了：" + Date().toString())
                    }

                })
//            }

            hnd0.postDelayed(this,20000)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMeetingRoomBinding.inflate(inflater, container, false)
        // ワードクラウド画面へ
//        binding.textWardClaud.setOnClickListener {
//            parentFragmentManager.beginTransaction()
//                .replace(R.id.nav_host_fragment_content_main, WordCloudFragment())
//                .commit()
//        }

        // インスタンスの生成：ここで渡しているcontextについてまだよくわかっていない
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(binding.root.context)
        // 音声認識結果を画面に表示：コールバック（リスナー？）の設定
        speechRecognizer?.setRecognitionListener(createRecognitionListenerStringStream {
//            binding.recognizeTextView.text = it
//            Log.d("音声認識結果", it)
        })
        // ミーティング画面に入ったら、音声入力スタート
        speechRecognizer?.startListening(speechRecognizerIntent)
        resultList.removeAt(0)


        return binding.root
    }

    override fun onStart() {
        super.onStart()
        // ミーティング画面に入ったら、端末本体の通知音をミュート
        mAudioManager = context?.getSystemService(AUDIO_SERVICE) as AudioManager
        mAudioManager.adjustStreamVolume(AudioManager.STREAM_NOTIFICATION, AudioManager.ADJUST_MUTE,0)
        hnd0.post(rnb0)
        binding.textTopicStatus.setBackgroundResource(R.drawable.kenti_signal_bar_default)
        binding.textTopicStatus.setText("トピックの変化はありません")
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as AppCompatActivity).supportActionBar?.title = "ミーティング画面"
    }

    override fun onPause() {
        super.onPause()
        speechRecognizer?.destroy()
        mAudioManager.adjustStreamVolume(AudioManager.STREAM_NOTIFICATION, AudioManager.ADJUST_UNMUTE,0)
        Log.d("MeetingRoomFragment", "onPause内 speechRecognizerインスタンスの破棄")
        Log.d("MeetingRoomFragment", resultList.toString())

    }


    override fun onStop() {
        super.onStop()
        speechRecognizer?.destroy()
        mAudioManager.adjustStreamVolume(AudioManager.STREAM_NOTIFICATION, AudioManager.ADJUST_UNMUTE,0)
        Log.d("MeetingRoomFragment", "onStop内 speechRecognizerインスタンスの破棄")
    }

    override fun onDestroy() {
        super.onDestroy()
        // speechRecognizerインスタンスの破棄
        speechRecognizer?.destroy()
        mAudioManager.adjustStreamVolume(AudioManager.STREAM_NOTIFICATION, AudioManager.ADJUST_UNMUTE, 0)
        Log.d("MeetingRoomFragment", "onDestroy内 speechRecognizerインスタンスの破棄")
        _binding = null
        hnd0.removeCallbacks(rnb0)
    }

    // onResultは高階関数、引数にStringを受け取りUnit型を返す（つまり戻り値なし？）
    private fun createRecognitionListenerStringStream(onResult : (String)-> Unit) : RecognitionListener {
        return object : RecognitionListener {
            // 音声のレベル？が変化したとき呼び出される
            override fun onRmsChanged(rmsdB: Float) {}
            // 準備が整い？発話しても良くなったとき呼ばれる
            override fun onReadyForSpeech(params: Bundle) {}
            // 音声が受信？できたとき呼ばれる
            override fun onBufferReceived(buffer: ByteArray) {}
            // 部分的な認識結果が利用可能なときに呼び出される？
            override fun onPartialResults(partialResults: Bundle) {}
            // 追加イベント？を受信？したとき呼ばれる
            override fun onEvent(eventType: Int, params: Bundle) {}
            // 発話を始めたとき呼ばれる
            override fun onBeginningOfSpeech() {}
            // 発話が終わったとき呼ばれる
            override fun onEndOfSpeech() {}
            // ネットワークまたは音声入力に関するエラーが発生したとき呼ばれる
            override fun onError(error: Int) {
                //onResult("onError")
                // 各エラーメッセージを表示
                val errorMessage = when (error) {
                    SpeechRecognizer.ERROR_AUDIO -> "ERROR_AUDIO"
                    SpeechRecognizer.ERROR_CLIENT -> "ERROR_CLIENT"
                    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "ERROR_INSUFFICIENT_PERMISSIONS"
                    SpeechRecognizer.ERROR_NETWORK -> "ERROR_NETWORK"
                    SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "ERROR_NETWORK_TIMEOUT"
                    SpeechRecognizer.ERROR_NO_MATCH ->  "ERROR_NO_MATCH"
                    SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "ERROR_RECOGNIZER_BUSY"
                    SpeechRecognizer.ERROR_SERVER -> "ERROR_SERVER"
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "ERROR_SPEECH_TIMEOUT"
                    SpeechRecognizer.ERROR_TOO_MANY_REQUESTS -> "ERROR_TOO_MANY_REQUESTS"
                    SpeechRecognizer.ERROR_LANGUAGE_NOT_SUPPORTED -> "ERROR_LANGUAGE_NOT_SUPPORTED"
                    SpeechRecognizer.ERROR_LANGUAGE_UNAVAILABLE -> "ERROR_LANGUAGE_UNAVAILABLE"
                    SpeechRecognizer.ERROR_SERVER_DISCONNECTED -> "ERROR_SERVER_DISCONNECTED"
                    else -> "Unknown error"
                }
                //onResult("onError : " + errorMessage)
                // エラーメッセージの表示
//                binding.errorMessageTextView.text = "ERROR : ${errorMessage}"
                Log.d("onError", "ERROR : ${errorMessage}")
                // エラーの場合、次の音声入力を開始
                speechRecognizer?.startListening(speechRecognizerIntent)
            }
            // 音声入力が終了後、結果？が準備できたら呼ばれる
            override fun onResults(results: Bundle) {
                val stringArray = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)

                // 過去の認識結果を4つまで保持, 認識結果の候補が複数個ある場合は最初の1つを使う(仮機能)
                resultList.add(stringArray?.get(0).toString())
                //resultList.add(stringArray.toString())
                if (resultList.count() >= 1){
//                    resultList.removeAt(0)
                    resultText = resultList.joinToString("。")
                    resultList.clear()
                    firestoreWriteData()
                }
                var recognizeText = ""
                resultList.forEach {
                    recognizeText += it + "\n"
                }
                // 画面に表示
                onResult(recognizeText)
                Log.d("onResults", "音声認識結果 : ${stringArray.toString()}")

                //エラーメッセージの削除
//                binding.errorMessageTextView.text = "ERROR : "

                // 次の音声入力の開始
                speechRecognizer?.startListening(speechRecognizerIntent)
            }
        }
    }

    private fun firestoreWriteData(){
        val db = FirebaseFirestore.getInstance()
        // 適当にmutableMapにしたけどどうなんだろ
        val texts = mutableMapOf("text" to resultText, "user" to "Android-TestUser")
        db.collection("Tsutsumi_Logic")
            .document("Room00")
            .update("VoiceData", FieldValue.arrayUnion(texts))  // Textsフィールドに配列要素として先に生成したマップを追加
            .addOnSuccessListener({
                Log.d("MainActivity", "送信完了")
            })
            .addOnFailureListener({
                Log.d("MainActivity", "送信失敗")
            })
    }
}