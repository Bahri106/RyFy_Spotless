package com.bahri.ryfy.core

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import java.util.Calendar
import java.util.Locale

object RyFyStrings {

    enum class Key {
        // Nav
        NAV_HOME, NAV_SEARCH, NAV_LIBRARY, NAV_SETTINGS,

        // Home
        GENRE_ALL, GENRE_POP, GENRE_INDIE, GENRE_ROCK, GENRE_ACOUSTIC, GENRE_KPOP, GENRE_CHILL,
        RECENTLY_PLAYED, MADE_FOR_YOU, TOP_CHARTS, RECOMMENDED_STATIONS,
        BEST_CHOICE_OF,

        // Search
        SEARCH_TITLE, SEARCH_PLACEHOLDER, RECENT_SEARCHES, CLEAR_ALL,

        // Library
        LIBRARY_TITLE, LIKED_SONGS, OFFLINE_SONGS, OFFLINE_SONGS_SUB,
        YOUR_PLAYLISTS, NO_PLAYLISTS, CREATE_PLAYLIST, OPEN_SHARED_PLAYLIST,
        PLAY_ALL, SHARE_PLAYLIST, DELETE_PLAYLIST,

        // Settings
        SETTINGS_TITLE, DEV_TEAM, DEV_TEAM_SUB, ABOUT_APP, ABOUT_APP_SUB,
        EQUALIZER_TITLE, EQUALIZER_SUB, PRIVACY_TITLE, PRIVACY_SUB,
        CLEAR_CACHE, CLEAR_CACHE_SUB, APP_LANGUAGE, APP_UPDATE,

        // Player & 3-Dots
        LYRICS_HEADER, LYRICS_OPEN_FULL, NO_LYRICS,
        LIKE_SONG, UNLIKE_SONG, ADD_TO_PLAYLIST,
        OPEN_QUEUE, ARTIST_WORKS, DOWNLOAD_OFFLINE, DELETE_OFFLINE, DOWNLOADING,
        SLEEP_TIMER, PLAYBACK_SPEED, SHARE_SONG,
        CLOSE, CANCEL
    }

    @Composable
    fun get(key: Key): String {
        val currentCode by AppLanguageManager.currentLanguage.collectAsState()
        val lang = if (currentCode == AppLanguageManager.LANG_SYSTEM) {
            val sys = Locale.getDefault().language.lowercase()
            if (sys in listOf("in", "id")) "in" else sys
        } else {
            currentCode
        }
        return translate(key, lang)
    }

    fun getGreeting(langCode: String): String {
        val lang = if (langCode == AppLanguageManager.LANG_SYSTEM) {
            val sys = Locale.getDefault().language.lowercase()
            if (sys in listOf("in", "id")) "in" else sys
        } else {
            langCode
        }
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return when (lang) {
            "zh" -> when (hour) {
                in 5..11 -> "早上好"
                in 12..17 -> "下午好"
                else -> "晚上好"
            }
            "ja" -> when (hour) {
                in 5..10 -> "おはようございます"
                in 11..17 -> "こんにちは"
                else -> "こんばんは"
            }
            "es" -> when (hour) {
                in 5..11 -> "Buenos días"
                in 12..17 -> "Buenas tardes"
                else -> "Buenas noches"
            }
            "ru" -> when (hour) {
                in 5..11 -> "Доброе утро"
                in 12..17 -> "Добрый день"
                else -> "Добрый вечер"
            }
            "ar" -> when (hour) {
                in 5..11 -> "صباح الخير"
                in 12..17 -> "مساء الخير"
                else -> "مساء النور"
            }
            "en" -> when (hour) {
                in 5..11 -> "Good morning"
                in 12..17 -> "Good afternoon"
                else -> "Good evening"
            }
            else -> when (hour) {
                in 4..11 -> "Selamat pagi"
                in 12..15 -> "Selamat siang"
                in 16..18 -> "Selamat sore"
                else -> "Selamat malam"
            }
        }
    }

    private fun translate(key: Key, lang: String): String {
        return when (lang) {
            "zh" -> when (key) {
                Key.NAV_HOME -> "首页"
                Key.NAV_SEARCH -> "搜索"
                Key.NAV_LIBRARY -> "音乐库"
                Key.NAV_SETTINGS -> "设置"

                Key.GENRE_ALL -> "全部"
                Key.GENRE_POP -> "流行"
                Key.GENRE_INDIE -> "独立"
                Key.GENRE_ROCK -> "摇滚"
                Key.GENRE_ACOUSTIC -> "原声"
                Key.GENRE_KPOP -> "韩流"
                Key.GENRE_CHILL -> "放松"

                Key.RECENTLY_PLAYED -> "最近播放"
                Key.MADE_FOR_YOU -> "为你打造"
                Key.TOP_CHARTS -> "热门榜单"
                Key.RECOMMENDED_STATIONS -> "推荐电台"
                Key.BEST_CHOICE_OF -> "精选推荐"

                Key.SEARCH_TITLE -> "搜索"
                Key.SEARCH_PLACEHOLDER -> "想听什么歌曲或歌手？"
                Key.RECENT_SEARCHES -> "最近搜索"
                Key.CLEAR_ALL -> "清空"

                Key.LIBRARY_TITLE -> "你的音乐库"
                Key.LIKED_SONGS -> "喜欢的歌曲"
                Key.OFFLINE_SONGS -> "离线已下载"
                Key.OFFLINE_SONGS_SUB -> "首歌曲 · 免流量离线畅听"
                Key.YOUR_PLAYLISTS -> "你的播放列表"
                Key.NO_PLAYLISTS -> "还没有播放列表，点击右上角加号创建。"
                Key.CREATE_PLAYLIST -> "新建播放列表"
                Key.OPEN_SHARED_PLAYLIST -> "打开共享播放列表"
                Key.PLAY_ALL -> "全部播放"
                Key.SHARE_PLAYLIST -> "分享歌单"
                Key.DELETE_PLAYLIST -> "删除歌单"

                Key.SETTINGS_TITLE -> "设置"
                Key.DEV_TEAM -> "开发团队与社区"
                Key.DEV_TEAM_SUB -> "Saiful Bahri (创始人) · Diki Dev (联合创始人)"
                Key.ABOUT_APP -> "关于 RyFy Spotless"
                Key.ABOUT_APP_SUB -> "应用详情、100%无广告与隐私说明"
                Key.EQUALIZER_TITLE -> "均衡器与音效"
                Key.EQUALIZER_SUB -> "重低音增强、音乐预设与5段滑块"
                Key.PRIVACY_TITLE -> "隐私与安全"
                Key.PRIVACY_SUB -> "无第三方追踪，免登录安全模式"
                Key.CLEAR_CACHE -> "清除音频缓存"
                Key.CLEAR_CACHE_SUB -> "释放临时存储空间"
                Key.APP_LANGUAGE -> "应用语言 / App Language"
                Key.APP_UPDATE -> "应用更新"

                Key.LYRICS_HEADER -> "歌词"
                Key.LYRICS_OPEN_FULL -> "全屏查看"
                Key.NO_LYRICS -> "此歌曲暂无歌词。"
                Key.LIKE_SONG -> "喜欢这首歌"
                Key.UNLIKE_SONG -> "从喜欢中移除"
                Key.ADD_TO_PLAYLIST -> "添加到播放列表"
                Key.OPEN_QUEUE -> "打开播放队列"
                Key.ARTIST_WORKS -> "该歌手的其他作品"
                Key.DOWNLOAD_OFFLINE -> "下载到离线"
                Key.DELETE_OFFLINE -> "删除离线文件"
                Key.DOWNLOADING -> "正在下载..."
                Key.SLEEP_TIMER -> "睡眠定时器"
                Key.PLAYBACK_SPEED -> "播放速度 (慢放 / 变奏)"
                Key.SHARE_SONG -> "分享这首歌"
                Key.CLOSE -> "关闭"
                Key.CANCEL -> "取消"
            }

            "ja" -> when (key) {
                Key.NAV_HOME -> "ホーム"
                Key.NAV_SEARCH -> "検索"
                Key.NAV_LIBRARY -> "マイライブラリ"
                Key.NAV_SETTINGS -> "設定"

                Key.GENRE_ALL -> "すべて"
                Key.GENRE_POP -> "ポップ"
                Key.GENRE_INDIE -> "インディーズ"
                Key.GENRE_ROCK -> "ロック"
                Key.GENRE_ACOUSTIC -> "アコースティック"
                Key.GENRE_KPOP -> "K-POP"
                Key.GENRE_CHILL -> "チル"

                Key.RECENTLY_PLAYED -> "最近再生した曲"
                Key.MADE_FOR_YOU -> "あなたへのおすすめ"
                Key.TOP_CHARTS -> "人気ランキング"
                Key.RECOMMENDED_STATIONS -> "おすすめステーション"
                Key.BEST_CHOICE_OF -> "ベストセレクション"

                Key.SEARCH_TITLE -> "検索"
                Key.SEARCH_PLACEHOLDER -> "聴きたい曲やアーティストは？"
                Key.RECENT_SEARCHES -> "最近の検索"
                Key.CLEAR_ALL -> "すべて削除"

                Key.LIBRARY_TITLE -> "マイライブラリ"
                Key.LIKED_SONGS -> "お気に入りの曲"
                Key.OFFLINE_SONGS -> "オフライン保存済み"
                Key.OFFLINE_SONGS_SUB -> "曲 · オフライン再生可能"
                Key.YOUR_PLAYLISTS -> "プレイリスト"
                Key.NO_PLAYLISTS -> "プレイリストがありません。"
                Key.CREATE_PLAYLIST -> "プレイリストを作成"
                Key.OPEN_SHARED_PLAYLIST -> "共有プレイリストを開く"
                Key.PLAY_ALL -> "すべて再生"
                Key.SHARE_PLAYLIST -> "プレイリストを共有"
                Key.DELETE_PLAYLIST -> "プレイリストを削除"

                Key.SETTINGS_TITLE -> "設定"
                Key.DEV_TEAM -> "開発チーム & コミュニティ"
                Key.DEV_TEAM_SUB -> "Saiful Bahri (創設者) · Diki Dev (共同創設者)"
                Key.ABOUT_APP -> "RyFy Spotless について"
                Key.ABOUT_APP_SUB -> "アプリ情報、完全広告なし & プライバシー"
                Key.EQUALIZER_TITLE -> "イコライザー & 音響効果"
                Key.EQUALIZER_SUB -> "低音ブースト、プリセット、5バンド調整"
                Key.PRIVACY_TITLE -> "プライバシー & セキュリティ"
                Key.PRIVACY_SUB -> "トラッカーなし、ログイン不要のゲストモード"
                Key.CLEAR_CACHE -> "キャッシュを削除"
                Key.CLEAR_CACHE_SUB -> "ストレージを解放"
                Key.APP_LANGUAGE -> "アプリの言語 / Language"
                Key.APP_UPDATE -> "アプリの更新"

                Key.LYRICS_HEADER -> "歌詞"
                Key.LYRICS_OPEN_FULL -> "全画面表示"
                Key.NO_LYRICS -> "この曲の歌詞はありません。"
                Key.LIKE_SONG -> "お気に入りに追加"
                Key.UNLIKE_SONG -> "お気に入りから削除"
                Key.ADD_TO_PLAYLIST -> "プレイリストに追加"
                Key.OPEN_QUEUE -> "再生キューを開く"
                Key.ARTIST_WORKS -> "アーティストの他の作品"
                Key.DOWNLOAD_OFFLINE -> "オフライン保存"
                Key.DELETE_OFFLINE -> "オフライン削除"
                Key.DOWNLOADING -> "ダウンロード中..."
                Key.SLEEP_TIMER -> "スリープタイマー"
                Key.PLAYBACK_SPEED -> "再生速度 (スロー / 倍速)"
                Key.SHARE_SONG -> "曲を共有"
                Key.CLOSE -> "閉じる"
                Key.CANCEL -> "キャンセル"
            }

            "en" -> when (key) {
                Key.NAV_HOME -> "Home"
                Key.NAV_SEARCH -> "Search"
                Key.NAV_LIBRARY -> "Your Library"
                Key.NAV_SETTINGS -> "Settings"

                Key.GENRE_ALL -> "All"
                Key.GENRE_POP -> "Pop"
                Key.GENRE_INDIE -> "Indie"
                Key.GENRE_ROCK -> "Rock"
                Key.GENRE_ACOUSTIC -> "Acoustic"
                Key.GENRE_KPOP -> "K-Pop"
                Key.GENRE_CHILL -> "Chill"

                Key.RECENTLY_PLAYED -> "Recently played"
                Key.MADE_FOR_YOU -> "Made For You"
                Key.TOP_CHARTS -> "Top Charts"
                Key.RECOMMENDED_STATIONS -> "Recommended Stations"
                Key.BEST_CHOICE_OF -> "Best of"

                Key.SEARCH_TITLE -> "Search"
                Key.SEARCH_PLACEHOLDER -> "What do you want to listen to?"
                Key.RECENT_SEARCHES -> "Recent searches"
                Key.CLEAR_ALL -> "Clear all"

                Key.LIBRARY_TITLE -> "Your Library"
                Key.LIKED_SONGS -> "Liked Songs"
                Key.OFFLINE_SONGS -> "Downloaded Music"
                Key.OFFLINE_SONGS_SUB -> "songs · Available offline"
                Key.YOUR_PLAYLISTS -> "Your Playlists"
                Key.NO_PLAYLISTS -> "No custom playlists yet. Tap '+' to create one."
                Key.CREATE_PLAYLIST -> "Create Playlist"
                Key.OPEN_SHARED_PLAYLIST -> "Open Shared Playlist"
                Key.PLAY_ALL -> "Play All"
                Key.SHARE_PLAYLIST -> "Share Playlist"
                Key.DELETE_PLAYLIST -> "Delete Playlist"

                Key.SETTINGS_TITLE -> "Settings"
                Key.DEV_TEAM -> "Developer Team & Community"
                Key.DEV_TEAM_SUB -> "Saiful Bahri (Founder) · Diki Dev (Co-Founder)"
                Key.ABOUT_APP -> "About RyFy Spotless"
                Key.ABOUT_APP_SUB -> "App info, 100% ad-free & privacy details"
                Key.EQUALIZER_TITLE -> "Equalizer & Sound Effects"
                Key.EQUALIZER_SUB -> "Bass Boost, Genre Presets & 5-Band Slider"
                Key.PRIVACY_TITLE -> "Privacy & Protection"
                Key.PRIVACY_SUB -> "No tracking, guest-mode zero login"
                Key.CLEAR_CACHE -> "Clear Audio Cache"
                Key.CLEAR_CACHE_SUB -> "Free up temporary storage"
                Key.APP_LANGUAGE -> "App Language / Bahasa"
                Key.APP_UPDATE -> "App Updates"

                Key.LYRICS_HEADER -> "LYRICS"
                Key.LYRICS_OPEN_FULL -> "OPEN FULLSCREEN"
                Key.NO_LYRICS -> "Lyrics are not available for this song."
                Key.LIKE_SONG -> "Like this song"
                Key.UNLIKE_SONG -> "Remove from Liked Songs"
                Key.ADD_TO_PLAYLIST -> "Add to Playlist"
                Key.OPEN_QUEUE -> "Open Playback Queue"
                Key.ARTIST_WORKS -> "More from artist"
                Key.DOWNLOAD_OFFLINE -> "Download Offline"
                Key.DELETE_OFFLINE -> "Delete Offline File"
                Key.DOWNLOADING -> "Downloading..."
                Key.SLEEP_TIMER -> "Sleep Timer"
                Key.PLAYBACK_SPEED -> "Playback Speed (Slowed / Speed)"
                Key.SHARE_SONG -> "Share Track"
                Key.CLOSE -> "Close"
                Key.CANCEL -> "Cancel"
            }

            "es" -> when (key) {
                Key.NAV_HOME -> "Inicio"
                Key.NAV_SEARCH -> "Buscar"
                Key.NAV_LIBRARY -> "Tu Biblioteca"
                Key.NAV_SETTINGS -> "Configuración"

                Key.GENRE_ALL -> "Todo"
                Key.GENRE_POP -> "Pop"
                Key.GENRE_INDIE -> "Indie"
                Key.GENRE_ROCK -> "Rock"
                Key.GENRE_ACOUSTIC -> "Acústico"
                Key.GENRE_KPOP -> "K-Pop"
                Key.GENRE_CHILL -> "Chill"

                Key.RECENTLY_PLAYED -> "Escuchado recientemente"
                Key.MADE_FOR_YOU -> "Hecho para ti"
                Key.TOP_CHARTS -> "Éxitos principales"
                Key.RECOMMENDED_STATIONS -> "Estaciones recomendadas"
                Key.BEST_CHOICE_OF -> "Lo mejor de"

                Key.SEARCH_TITLE -> "Buscar"
                Key.SEARCH_PLACEHOLDER -> "¿Qué quieres escuchar?"
                Key.RECENT_SEARCHES -> "Búsquedas recientes"
                Key.CLEAR_ALL -> "Borrar todo"

                Key.LIBRARY_TITLE -> "Tu Biblioteca"
                Key.LIKED_SONGS -> "Canciones que te gustan"
                Key.OFFLINE_SONGS -> "Música descargada"
                Key.OFFLINE_SONGS_SUB -> "canciones · Disponible sin internet"
                Key.YOUR_PLAYLISTS -> "Tus Playlists"
                Key.NO_PLAYLISTS -> "Aún no tienes playlists creadas."
                Key.CREATE_PLAYLIST -> "Crear Playlist"
                Key.OPEN_SHARED_PLAYLIST -> "Abrir Playlist compartida"
                Key.PLAY_ALL -> "Reproducir todo"
                Key.SHARE_PLAYLIST -> "Compartir Playlist"
                Key.DELETE_PLAYLIST -> "Eliminar Playlist"

                Key.SETTINGS_TITLE -> "Configuración"
                Key.DEV_TEAM -> "Equipo de Desarrollo & Comunidad"
                Key.DEV_TEAM_SUB -> "Saiful Bahri (Fundador) · Diki Dev (Cofundador)"
                Key.ABOUT_APP -> "Acerca de RyFy Spotless"
                Key.ABOUT_APP_SUB -> "Info de la app, 100% sin anuncios y privacidad"
                Key.EQUALIZER_TITLE -> "Ecualizador y Efectos"
                Key.EQUALIZER_SUB -> "Refuerzo de graves, preajustes y 5 bandas"
                Key.PRIVACY_TITLE -> "Privacidad y Protección"
                Key.PRIVACY_SUB -> "Sin rastreo, modo invitado sin inicio de sesión"
                Key.CLEAR_CACHE -> "Limpiar caché de audio"
                Key.CLEAR_CACHE_SUB -> "Liberar espacio de almacenamiento"
                Key.APP_LANGUAGE -> "Idioma de la aplicación"
                Key.APP_UPDATE -> "Actualizaciones de la app"

                Key.LYRICS_HEADER -> "LETRAS"
                Key.LYRICS_OPEN_FULL -> "PANTALLA COMPLETA"
                Key.NO_LYRICS -> "Letras no disponibles para esta canción."
                Key.LIKE_SONG -> "Me gusta esta canción"
                Key.UNLIKE_SONG -> "Eliminar de Me gusta"
                Key.ADD_TO_PLAYLIST -> "Añadir a Playlist"
                Key.OPEN_QUEUE -> "Abrir cola de reproducción"
                Key.ARTIST_WORKS -> "Más canciones del artista"
                Key.DOWNLOAD_OFFLINE -> "Descargar sin conexión"
                Key.DELETE_OFFLINE -> "Eliminar descarga"
                Key.DOWNLOADING -> "Descargando..."
                Key.SLEEP_TIMER -> "Temporizador de apagado"
                Key.PLAYBACK_SPEED -> "Velocidad de reproducción"
                Key.SHARE_SONG -> "Compartir canción"
                Key.CLOSE -> "Cerrar"
                Key.CANCEL -> "Cancelar"
            }

            else -> when (key) { // "in" / default Indonesian
                Key.NAV_HOME -> "Home"
                Key.NAV_SEARCH -> "Search"
                Key.NAV_LIBRARY -> "Library"
                Key.NAV_SETTINGS -> "Settings"

                Key.GENRE_ALL -> "Semua"
                Key.GENRE_POP -> "Pop"
                Key.GENRE_INDIE -> "Indie"
                Key.GENRE_ROCK -> "Rock"
                Key.GENRE_ACOUSTIC -> "Akustik"
                Key.GENRE_KPOP -> "K-Pop"
                Key.GENRE_CHILL -> "Chill"

                Key.RECENTLY_PLAYED -> "Baru saja diputar"
                Key.MADE_FOR_YOU -> "Dibuat untuk Anda"
                Key.TOP_CHARTS -> "Tangga Lagu Teratas"
                Key.RECOMMENDED_STATIONS -> "Stasiun rekomendasi"
                Key.BEST_CHOICE_OF -> "Pilihan Terbaik"

                Key.SEARCH_TITLE -> "Pencarian"
                Key.SEARCH_PLACEHOLDER -> "Apa yang ingin kamu dengarkan?"
                Key.RECENT_SEARCHES -> "Pencarian Terbaru"
                Key.CLEAR_ALL -> "Hapus semua"

                Key.LIBRARY_TITLE -> "Koleksi Kamu"
                Key.LIKED_SONGS -> "Lagu yang Disukai"
                Key.OFFLINE_SONGS -> "Musik Tersimpan Offline"
                Key.OFFLINE_SONGS_SUB -> "lagu · Tersedia tanpa internet"
                Key.YOUR_PLAYLISTS -> "Playlist Kamu"
                Key.NO_PLAYLISTS -> "Belum ada playlist kustom. Ketuk ikon '+' untuk membuat baru."
                Key.CREATE_PLAYLIST -> "Buat Playlist Baru"
                Key.OPEN_SHARED_PLAYLIST -> "Buka Playlist Bersama"
                Key.PLAY_ALL -> "Putar Semua"
                Key.SHARE_PLAYLIST -> "Bagikan Playlist"
                Key.DELETE_PLAYLIST -> "Hapus Playlist"

                Key.SETTINGS_TITLE -> "Pengaturan"
                Key.DEV_TEAM -> "Tim Pengembang & Komunitas"
                Key.DEV_TEAM_SUB -> "Saiful Bahri (Founder) · Diki Dev (Co-Founder)"
                Key.ABOUT_APP -> "Tentang RyFy Spotless"
                Key.ABOUT_APP_SUB -> "Informasi aplikasi, fitur bebas iklan & privasi"
                Key.EQUALIZER_TITLE -> "Equalizer & Efek Suara"
                Key.EQUALIZER_SUB -> "Penguat Bass (Bass Boost), Preset Musik, & 5-Band Slider"
                Key.PRIVACY_TITLE -> "Proteksi & Privasi"
                Key.PRIVACY_SUB -> "Tanpa pelacak, mode tamu zero-login aman"
                Key.CLEAR_CACHE -> "Bersihkan Cache Audio"
                Key.CLEAR_CACHE_SUB -> "Kosongkan ruang penyimpanan sementara"
                Key.APP_LANGUAGE -> "Bahasa Aplikasi / App Language"
                Key.APP_UPDATE -> "Pembaruan Aplikasi"

                Key.LYRICS_HEADER -> "LIRIK"
                Key.LYRICS_OPEN_FULL -> "BUKA PENUH"
                Key.NO_LYRICS -> "Lirik untuk lagu ini belum tersedia."
                Key.LIKE_SONG -> "Sukai Lagu Ini"
                Key.UNLIKE_SONG -> "Hapus dari Lagu yang Disukai"
                Key.ADD_TO_PLAYLIST -> "Tambahkan ke Playlist"
                Key.OPEN_QUEUE -> "Buka Antrean Pemutaran"
                Key.ARTIST_WORKS -> "Karya Lain dari Artis Ini"
                Key.DOWNLOAD_OFFLINE -> "Unduh Lagu Offline"
                Key.DELETE_OFFLINE -> "Hapus Lagu Offline"
                Key.DOWNLOADING -> "Sedang Mengunduh..."
                Key.SLEEP_TIMER -> "Pengatur Waktu Tidur"
                Key.PLAYBACK_SPEED -> "Kecepatan Pemutaran (Slowed / Speed)"
                Key.SHARE_SONG -> "Bagikan Lagu"
                Key.CLOSE -> "Tutup"
                Key.CANCEL -> "Batal"
            }
        }
    }
}
