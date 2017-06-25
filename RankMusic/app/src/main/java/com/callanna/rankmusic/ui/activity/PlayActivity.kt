package com.callanna.rankmusic.ui.activity

import android.app.Activity
import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Build
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.ImageView
import android.widget.SeekBar
import com.callanna.rankmusic.App
import com.callanna.rankmusic.R
import com.callanna.rankmusic.bean.Music
import com.callanna.rankmusic.dagger.compontent.PlayMusicModule
import com.callanna.rankmusic.databinding.ActivityPlayBinding
import com.callanna.rankmusic.db.MusicDBManager
import com.callanna.rankmusic.mvp.contract.PlayContract
import com.callanna.rankmusic.mvp.presenter.PlayPresenter
import com.callanna.rankmusic.ui.activity.base.BaseBindingActivity
import com.callanna.rankmusic.ui.fragment.play.LrcFragment
import com.callanna.rankmusic.ui.fragment.play.MusicListFragment
import com.callanna.rankmusic.util.*
import kotlinx.android.synthetic.main.activity_play.*
import javax.inject.Inject

class PlayActivity : BaseBindingActivity<ActivityPlayBinding>(), PlayContract.View {


    private val musiclistFragment: MusicListFragment = MusicListFragment.newInstance()

    private val lrcFragment: LrcFragment = LrcFragment.newInstance()


    @Inject lateinit var mPresenter: PlayPresenter
    override fun initView(savedInstanceState: Bundle?) {
        if(savedInstanceState!= null){
            mBinding.type = savedInstanceState.getString(TYPE)
            App.playCurrentType = mBinding.type
        }else {
            mBinding.type = intent.getStringExtra(TYPE)
            App.playCurrentType = mBinding.type
        }
        mBinding.root.setOnClickListener {
            supportFinishAfterTransition()
        }
        setupToolbar(toolbar)
        getMainComponent().plus(PlayMusicModule(this)).inject(this)
        musiclistFragment.setPresenter(mPresenter)
        lrcFragment.setPresenter(mPresenter)

        if (mBinding.type == Constants.SEARCH) {
            mPresenter.searchByKey(intent.getStringExtra(KEYWORD))
        } else {
            mPresenter.getSongList(mBinding.type)
        }
        seekbar_voice.max = SoundUtils.getInstance(context).soundMax
        seekbar_voice.progress = SoundUtils.getInstance(context).soundValue
        cb_voice.isChecked = SoundUtils.getInstance(context).isSilence
        seekbar_voice.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    SoundUtils.getInstance(context).setStreamVolume(progress)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                SoundUtils.getInstance(context).setStreamVolume(seekbar_voice.progress)
                cb_voice.isChecked = SoundUtils.getInstance(context).isSilence
            }
        })

        cb_voice.setOnClickListener {
            if (cb_voice.isChecked) {
                SoundUtils.getInstance(context).setSilentMode(true)
            } else {
                SoundUtils.getInstance(context).setSilentMode(false)
            }
            seekbar_voice.progress = SoundUtils.getInstance(context).soundValue

        }
        btn_next.setOnClickListener {
            mPresenter.next()
        }
        btn_pre.setOnClickListener {
            mPresenter.pre()
        }
        btn_play.setOnClickListener {
            if (MediaPlayerUtil.instance.isPlaying()) {
                mPresenter.pause()
            } else {
                mPresenter.start()
            }
        }
        cb_word.setOnClickListener {
            if (cb_word.isChecked) {
                addFragments(R.id.layout_content,lrcFragment)
               // MainContentUtil.getInstance().addMainContent(lrcFragment, false)
            } else {
                addFragments(R.id.layout_content,musiclistFragment)
               // MainContentUtil.getInstance().addMainContent(musiclistFragment, false)
            }
        }
        seekbar_song.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    mPresenter.seekTo(seekbar_song.progress)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                mPresenter.seekTo(seekbar_song.progress)
            }
        })
        imv_playmode.setOnClickListener {
            mPresenter.changeMode()
        }

       // MainContentUtil.getInstance().init(this)
        //MainContentUtil.getInstance().addMainContent(lrcFragment, false)
       addFragments(R.id.layout_content,lrcFragment)
    }
    override fun createDataBinding(savedInstanceState: Bundle?): ActivityPlayBinding {
        return DataBindingUtil.setContentView(this, R.layout.activity_play)
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        outState!!.putString(TYPE,mBinding.type)
        super.onSaveInstanceState(outState)

    }



    override fun setSongList(result: List<Music>) {
        musiclistFragment.setData(result)
        val pos = intent.getIntExtra(POSITION, 0)
        if (MediaPlayerUtil.instance.isPlaying() && App.instance.currentType == mBinding.type && App.instance.currentSong != null) {
            setCurrentSong(App.instance.currentSong!!)
            val lrc = MusicDBManager.instance.selectById(App.instance.currentSong?.songid.toString())!!.lrc
            setSongLrc(lrc)
            start()
        } else {
            mPresenter.play(pos)
            mBinding.song = result[pos]
        }

    }

    override fun setSongLrc(lrc: String) {
        lrcFragment.setSongLrc(lrc)
    }

    override fun setCurrentSong(song: Music) {
        mBinding.song = song
        mBinding.executePendingBindings()
        musiclistFragment.update()

    }

    override fun stop() {
         runUI {
            btn_play.setBackgroundResource(R.mipmap.ic_stop)
        }
    }

    override fun start() {
        runUI {
            btn_play.setBackgroundResource(R.mipmap.ic_pause)
        }
    }

    override fun currentPlayTime(now: Int, total: Int) {
        runUI {
            val strnow = "0" + (now / 60) + ":" + if (now % 60 >= 10) now % 60 else "0" + now % 60
            val strtotal = "0" + (total / 60) + ":" + if (total % 60 >= 10) total % 60 else "0" + total % 60
            //tv_song_time.text = strnow + "/" + strtotal
            tv_song_currenttime.text = strnow
            tv_song_totalttime.text = strtotal
            seekbar_song.max = total
            seekbar_song.progress = now
            lrcFragment.update(now.toLong())
        }
    }

    override fun setMode(mode: Int) {
        runUI {
            when (mode) {
                Constants.MODE_ORDER ->
                    imv_playmode.setBackgroundResource(R.mipmap.ic_cycle)
                Constants.MODE_LOOP ->
                    imv_playmode.setBackgroundResource(R.mipmap.ic_radom)
                Constants.MODE_REPEAT ->
                    imv_playmode.setBackgroundResource(R.mipmap.ic_onice)
            }
        }
    }

    override fun onDestroy() {
        mPresenter.unSubscribe()
        mBinding.unbind()
        super.onDestroy()
    }

    companion object {
        val TYPE = "TYPE"
        val POSITION = "POSITION"
        val KEYWORD = "KEY"
        fun startActivity(context: Context, type: String, position: Int = 0, view: View = ImageView(context)) {
            val intent = Intent(context, PlayActivity::class.java)
            intent.putExtra(TYPE, type)
            intent.putExtra(POSITION, position)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            if (Build.VERSION.SDK_INT > 21) {
                context.startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(context as AppCompatActivity, view, "img").toBundle())
            } else {
                context.startActivity(intent)
            }
        }

        fun searchByKey(context: Context, key: String = "", imageView: ImageView = ImageView(context)) {
            val intent = Intent(context, PlayActivity::class.java)
            intent.putExtra(TYPE, Constants.SEARCH)
            intent.putExtra(KEYWORD, key)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            if (Build.VERSION.SDK_INT > 21) {
                context.startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(context as Activity, imageView, "img").toBundle())
            } else {
                context.startActivity(intent)
            }
        }
    }

}
