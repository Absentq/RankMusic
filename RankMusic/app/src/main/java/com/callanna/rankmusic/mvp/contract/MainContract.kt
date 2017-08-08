package com.callanna.rankmusic.mvp.contract

import com.callanna.rankmusic.bean.Music
import com.callanna.rankmusic.bean.SongLrc
import com.callanna.rankmusic.util.Constants
import rx.Observable

/**
 * Created by Callanna on 2017/6/4.
 */
interface MainContract{
    interface View{
        fun setHotSong(result: List<Music>)
        fun setRock(result: List<Music>)
        fun setLocal(result: List<Music>)
        fun setUK(result: List<Music>)
        fun setKorea(result: List<Music>)
    }
    interface Model{
        fun getData(type :String):Observable<List<Music>>
        fun getSongLrc(songId:String):Observable<SongLrc>
        fun searchByKey(key:String):Observable<List<Music>>
        fun getMyMusic():Observable<List<Music>>
        fun getMyLove():Observable<List<Music>>
        fun getDownLoaded():Observable<List<Music>>
        fun getDownLoading():Observable<List<Music>>
        fun getHistory():Observable<List<Music>>
    }

    interface Presenter{
        fun getData(type :String = Constants.ALL)
    }
}