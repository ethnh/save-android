package net.opendasharchive.openarchive.features.backends

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import net.opendasharchive.openarchive.databinding.FragmentBackendSelectionBinding
import net.opendasharchive.openarchive.db.Backend

class BackendSelectionFragment : Fragment() {

    private lateinit var viewBinding: FragmentBackendSelectionBinding
    private lateinit var recyclerView: RecyclerView
    private val backendViewModel: BackendViewModel by activityViewModels()
    private val viewModel: BackendSelectionViewModel by viewModels()
    private val adapter = BackendSelectionAdapter { item ->
        useBackend(item.backend)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        viewBinding = FragmentBackendSelectionBinding.inflate(inflater)

        createBackendList()

        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                requireActivity().finish()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)
    }

        // We cannot browse the Internet Archive. Directly forward to creating a project,
        // as it doesn't make sense to show a one-option menu.
//        if (Folder.current?.backend?.tType == Backend.Type.INTERNET_ARCHIVE) {
//            mBinding.browseFolders.hide()
//            setFolder(false)
//            finish()
//        }

    private fun useBackend(backend: Backend) {
        if (backend.exists()) {
            findNavController().navigate(BackendSelectionFragmentDirections.navigateToCreateNewFolderScreen())
        } else {
            when (backend.tType) {
                Backend.Type.WEBDAV -> findNavController().navigate(BackendSelectionFragmentDirections.navigateToPrivateServerScreen())
                Backend.Type.INTERNET_ARCHIVE -> findNavController().navigate(BackendSelectionFragmentDirections.navigateToInternetArchiveScreen(backend, true))
                Backend.Type.GDRIVE -> findNavController().navigate(BackendSelectionFragmentDirections.navigateToGdriveScreen())
                Backend.Type.SNOWBIRD -> findNavController().navigate(BackendSelectionFragmentDirections.navigateToSnowbirdScreen())
                Backend.Type.FILECOIN -> findNavController().navigate(BackendSelectionFragmentDirections.navigateToWeb3Screen())
                else -> Unit
            }
        }
    }

    private fun createBackendList() {
        recyclerView = viewBinding.backendList

//        val spacingInPixels = resources.getDimensionPixelSize(R.dimen.list_item_spacing)
//        viewBinding.backendList.addItemDecoration(SpacingItemDecoration(spacingInPixels))

        viewBinding.backendList.layoutManager = LinearLayoutManager(requireContext())
        viewBinding.backendList.adapter = adapter

        viewModel.backends.observe(viewLifecycleOwner) { groupedBackends ->
            adapter.submitList(groupedBackends.toFlattenedList())
        }
    }

//    private fun removeInternetArchive() {
//        val backend = Backend.get(type = Backend.Type.INTERNET_ARCHIVE).firstOrNull()
//
//        if (backend != null) {
//            AlertHelper.show(
//                this,
//                R.string.are_you_sure_you_want_to_remove_this_server_from_the_app,
//                R.string.remove_from_app,
//                buttons = listOf(
//                    AlertHelper.positiveButton(R.string.remove) { _, _ ->
//                        backend.delete()
//                    },
//                    AlertHelper.negativeButton()
//                )
//            )
//        } else {
//            Timber.d("Unable to find backend.")
//        }
//    }
}