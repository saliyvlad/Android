package com.example.android

import android.app.Activity
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.File
import java.util.concurrent.TimeUnit

class MediaPlayerActivity : Activity() {

    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var seekBar: SeekBar
    private lateinit var volumeSeekBar: SeekBar
    private lateinit var btnPlayPause: Button
    private lateinit var btnPrevious: Button
    private lateinit var btnNext: Button
    private lateinit var tvCurrentTrack: TextView
    private lateinit var tvCurrentTime: TextView
    private lateinit var tvTotalTime: TextView
    private lateinit var lvTracks: ListView
    private lateinit var tvStatus: TextView

    private val handler = Handler(Looper.getMainLooper())
    private val updateSeekBar = object : Runnable {
        override fun run() {
            if (mediaPlayer.isPlaying) {
                val currentPosition = mediaPlayer.currentPosition
                seekBar.progress = currentPosition
                tvCurrentTime.text = formatTime(currentPosition)
            }
            handler.postDelayed(this, 1000)
        }
    }

    private var tracksList = mutableListOf<File>()
    private var currentTrackIndex = 0
    private val STORAGE_PERMISSION_CODE = 123

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_media_player)

        initializeViews()
        setupMediaPlayer()
        setupListeners()

        // Проверяем разрешения при создании
        if (!hasStoragePermission()) {
            requestStoragePermission()
        } else {
            loadAudioFiles()
        }
    }

    private fun initializeViews() {
        seekBar = findViewById(R.id.seekBar)
        volumeSeekBar = findViewById(R.id.volumeSeekBar)
        btnPlayPause = findViewById(R.id.btnPlayPause)
        btnPrevious = findViewById(R.id.btnPrevious)
        btnNext = findViewById(R.id.btnNext)
        tvCurrentTrack = findViewById(R.id.tvCurrentTrack)
        tvCurrentTime = findViewById(R.id.tvCurrentTime)
        tvTotalTime = findViewById(R.id.tvTotalTime)
        lvTracks = findViewById(R.id.lvTracks)

        // Добавляем TextView для статуса (добавьте в XML если нет)
        tvStatus = TextView(this).apply {
            text = "Проверка разрешений..."
        }
    }

    private fun setupMediaPlayer() {
        mediaPlayer = MediaPlayer()

        mediaPlayer.setOnCompletionListener {
            playNextTrack()
        }

        mediaPlayer.setOnPreparedListener {
            seekBar.max = mediaPlayer.duration
            tvTotalTime.text = formatTime(mediaPlayer.duration)
            handler.post(updateSeekBar)
        }
    }

    // Проверка разрешений для разных версий Android
    private fun hasStoragePermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ - новые разрешения
            ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.READ_MEDIA_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            // Android до 13 - старые разрешения
            ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    // Запрос соответствующих разрешений
    private fun requestStoragePermission() {
        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+
            arrayOf(android.Manifest.permission.READ_MEDIA_AUDIO)
        } else {
            // Android до 13
            arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        ActivityCompat.requestPermissions(
            this,
            permissions,
            STORAGE_PERMISSION_CODE
        )
    }

    // Обработка результата запроса разрешений
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            STORAGE_PERMISSION_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Разрешение получено
                    tvStatus.text = "Разрешение получено! Загружаем музыку..."
                    Toast.makeText(this, "Разрешение получено!", Toast.LENGTH_SHORT).show()
                    loadAudioFiles()
                } else {
                    // Разрешение отклонено
                    tvStatus.text = "Разрешение отклонено. Функциональность ограничена."
                    Toast.makeText(
                        this,
                        "Разрешение необходимо для доступа к музыке",
                        Toast.LENGTH_LONG
                    ).show()
                    enableDemoMode()
                }
            }
        }
    }

    private fun loadAudioFiles() {
        tracksList.clear()
        tvStatus.text = "Поиск аудиофайлов..."

        // Основные папки с музыкой
        val musicDirectories = arrayOf(
            File("/storage/emulated/0/Music"),
            File("/storage/emulated/0/Download"),
            File("/storage/emulated/0/DCIM/Music"),
            File("/storage/emulated/0/Audio"),
            File("/storage/emulated/0/Sounds")
        )

        var filesFound = 0
        for (directory in musicDirectories) {
            if (directory.exists() && directory.isDirectory) {
                filesFound += findAudioFiles(directory)
            }
        }

        if (filesFound > 0) {
            tvStatus.text = "Найдено $filesFound аудиофайлов"
            setupTracksList()
        } else {
            tvStatus.text = "Аудиофайлы не найдены"
            enableDemoMode()
        }
    }

    private fun findAudioFiles(directory: File): Int {
        var count = 0
        try {
            val files = directory.listFiles()
            files?.forEach { file ->
                if (file.isDirectory) {
                    // Рекурсивный поиск в подпапках
                    count += findAudioFiles(file)
                } else {
                    // Проверяем расширения аудиофайлов
                    val fileName = file.name.lowercase()
                    if (fileName.endsWith(".mp3") ||
                        fileName.endsWith(".wav") ||
                        fileName.endsWith(".ogg") ||
                        fileName.endsWith(".m4a") ||
                        fileName.endsWith(".flac")) {
                        tracksList.add(file)
                        count++
                    }
                }
            }
        } catch (e: SecurityException) {
            println("Нет доступа к директории: ${directory.absolutePath}")
        } catch (e: Exception) {
            println("Ошибка при сканировании: ${e.message}")
        }
        return count
    }
    class DemoAudioFile(private val demoName: String, private val demoDuration: Long) : File(demoName) {
        override fun exists(): Boolean = true
        override fun getName(): String = demoName  // Используем demoName вместо name
        override fun length(): Long = demoDuration // Используем demoDuration вместо duration
        override fun isFile(): Boolean = true
    }

    private fun enableDemoMode() {
        // Создаем демо-треки
        val demoFiles = listOf(
            DemoAudioFile("Демо трек 1 - Популярная музыка", 180000),
            DemoAudioFile("Демо трек 2 - Классическая музыка", 240000),
            DemoAudioFile("Демо трек 3 - Электронная музыка", 200000)
        )

        tracksList.addAll(demoFiles)
        setupTracksList()

        Toast.makeText(
            this,
            "Демо-режим. Для доступа к вашей музыке предоставьте разрешение.",
            Toast.LENGTH_LONG
        ).show()
    }

    private fun setupTracksList() {
        val trackNames = tracksList.map { file ->
            // Получаем имя через getName() для всех типов файлов
            file.name
        }

        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_1,
            trackNames
        )
        lvTracks.adapter = adapter

        lvTracks.setOnItemClickListener { _, _, position, _ ->
            if (hasStoragePermission() || tracksList[position] is DemoAudioFile) {
                playTrack(position)
            } else {
                Toast.makeText(
                    this,
                    "Необходимо разрешение для воспроизведения музыки",
                    Toast.LENGTH_SHORT
                ).show()
                requestStoragePermission()
            }
        }

        tvStatus.text = "Готово к воспроизведению. Треков: ${tracksList.size}"
    }

    private fun playTrack(position: Int) {
        try {
            mediaPlayer.reset()
            currentTrackIndex = position

            val trackFile = tracksList[position]

            if (trackFile is DemoAudioFile) {
                // Демо-режим - просто меняем текст
                tvCurrentTrack.text = "Демо: ${trackFile.name}" // Используем .name
                seekBar.max = trackFile.length().toInt()        // Используем .length()
                tvTotalTime.text = formatTime(trackFile.length().toInt())
                btnPlayPause.text = "▶"
                Toast.makeText(this, "Демо-режим: воспроизведение имитируется", Toast.LENGTH_SHORT).show()
            } else {
                // Реальный файл
                mediaPlayer.setDataSource(trackFile.absolutePath)
                mediaPlayer.prepareAsync()
                tvCurrentTrack.text = trackFile.name           // Используем .name
                btnPlayPause.text = "⏸"
            }

        } catch (e: Exception) {
            Toast.makeText(this, "Ошибка: ${e.message}", Toast.LENGTH_SHORT).show()
            tvStatus.text = "Ошибка воспроизведения"
        }
    }

    private fun setupListeners() {
        btnPlayPause.setOnClickListener {
            if (tracksList.isNotEmpty()) {
                if (mediaPlayer.isPlaying) {
                    mediaPlayer.pause()
                    btnPlayPause.text = "▶"
                    handler.removeCallbacks(updateSeekBar)
                } else {
                    if (tracksList[currentTrackIndex] !is DemoAudioFile) {
                        mediaPlayer.start()
                        btnPlayPause.text = "⏸"
                        handler.post(updateSeekBar)
                    } else {
                        Toast.makeText(this, "Демо-режим: воспроизведение невозможно", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        btnPrevious.setOnClickListener {
            playPreviousTrack()
        }

        btnNext.setOnClickListener {
            playNextTrack()
        }

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser && tracksList.isNotEmpty() && tracksList[currentTrackIndex] !is DemoAudioFile) {
                    mediaPlayer.seekTo(progress)
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        volumeSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    val volume = progress / 100.0f
                    mediaPlayer.setVolume(volume, volume)
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private fun playNextTrack() {
        if (tracksList.isNotEmpty()) {
            currentTrackIndex = (currentTrackIndex + 1) % tracksList.size
            playTrack(currentTrackIndex)
        }
    }

    private fun playPreviousTrack() {
        if (tracksList.isNotEmpty()) {
            currentTrackIndex = (currentTrackIndex - 1 + tracksList.size) % tracksList.size
            playTrack(currentTrackIndex)
        }
    }

    private fun formatTime(milliseconds: Int): String {
        val minutes = TimeUnit.MILLISECONDS.toMinutes(milliseconds.toLong())
        val seconds = TimeUnit.MILLISECONDS.toSeconds(milliseconds.toLong()) -
                TimeUnit.MINUTES.toSeconds(minutes)
        return String.format("%02d:%02d", minutes, seconds)
    }

    override fun onPause() {
        super.onPause()
        if (mediaPlayer.isPlaying) {
            mediaPlayer.pause()
            btnPlayPause.text = "▶"
        }
        handler.removeCallbacks(updateSeekBar)
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer.release()
        handler.removeCallbacks(updateSeekBar)
    }
}