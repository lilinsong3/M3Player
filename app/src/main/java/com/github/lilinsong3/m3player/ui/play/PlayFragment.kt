package com.github.lilinsong3.m3player.ui.play

import android.content.ComponentName
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.media3.session.MediaBrowser
import androidx.media3.session.SessionToken
import com.github.lilinsong3.m3player.databinding.TestBinding
import com.google.common.util.concurrent.ListenableFuture

class PlayFragment : Fragment() {

    private var _binding: TestBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PlayViewModel by viewModels()

    private lateinit var browserFuture: ListenableFuture<MediaBrowser>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = TestBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        val context = requireContext()
        browserFuture = MediaBrowser.Builder(
            context,
            SessionToken(context, ComponentName(context, AudioLibraryService::class.java))
        ).buildAsync()
        browserFuture.addListener(
            {
                binding.testPlayerView.player = browserFuture.get()
            },
            // MoreExecutors.directExecutor()
            ContextCompat.getMainExecutor(context)
        )
    }

    override fun onDestroyView() {
        binding.testPlayerView.player = null
        _binding = null
        super.onDestroyView()
    }

    override fun onStop() {
        MediaBrowser.releaseFuture(browserFuture)
        super.onStop()
    }
}