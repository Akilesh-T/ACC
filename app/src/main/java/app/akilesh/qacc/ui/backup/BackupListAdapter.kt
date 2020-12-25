package app.akilesh.qacc.ui.backup

import android.content.Context
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.RecyclerView
import app.akilesh.qacc.R
import app.akilesh.qacc.databinding.ItemBackupsBinding
import app.akilesh.qacc.model.BackupFile
import app.akilesh.qacc.model.Colour
import app.akilesh.qacc.utils.AppUtils.getColorAccent

class BackupListAdapter(
    val context: Context,
    private var filesList: MutableList<BackupFile>,
    private val preview: (List<Colour>) -> Unit,
    val restore: (String) -> Unit
) : RecyclerView.Adapter<BackupListAdapter.BackupsViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BackupsViewHolder
            = BackupsViewHolder.from(parent)

    override fun onBindViewHolder(holder: BackupsViewHolder, position: Int)
            = holder.bind(
                filesList[position],
                context,
                preview,
                restore
            )

    override fun getItemCount() = filesList.size

    class BackupsViewHolder private constructor(val binding: ItemBackupsBinding)
        : RecyclerView.ViewHolder(binding.root) {

        fun bind(
            backupFile: BackupFile,
            context: Context,
            preview: (List<Colour>) -> Unit,
            restore: (String) -> Unit
        ) {
            binding.backupFile.text = backupFile.fileName.removeSuffix(".tar.gz").replace('-', ' ')
            binding.fileSize.text = backupFile.fileSize
            binding.viewContent.text =
                context.getString(R.string.view_backup_contents, backupFile.noOfApps)
            binding.viewContent.setOnClickListener { preview(backupFile.colors) }
            binding.restore.setOnClickListener { restore(backupFile.fileName) }

            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
            val useSystemAccent = sharedPreferences.getBoolean("system_accent", false)
            if (useSystemAccent) {
                val colorStateList = ColorStateList.valueOf(context.getColorAccent())
                binding.viewContent.apply {
                    iconTint = colorStateList
                    setTextColor(colorStateList)
                    rippleColor = colorStateList
                }
                binding.restore.apply {
                    iconTint = colorStateList
                    setTextColor(colorStateList)
                    rippleColor = colorStateList
                }
            }
        }

        companion object {
            fun from(parent: ViewGroup): BackupsViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ItemBackupsBinding
                    .inflate(layoutInflater, parent, false)
                return BackupsViewHolder(binding)
            }
        }
    }

    fun setFiles(files: MutableList<BackupFile>) {
        this.filesList = files
        notifyDataSetChanged()
    }

    fun getFileAndRemoveAt(position: Int): String {
        val current = filesList[position]
        filesList.removeAt(position)
        notifyItemRemoved(position)
        return current.fileName
    }
}
