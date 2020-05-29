package app.akilesh.qacc.ui.backup

import android.content.Context
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.RecyclerView
import app.akilesh.qacc.R
import app.akilesh.qacc.databinding.RecyclerviewItemBackupsBinding
import app.akilesh.qacc.model.BackupFile
import app.akilesh.qacc.utils.AppUtils.getColorAccent

class BackupListAdapter internal constructor(
    val context: Context,
    private var filesList: MutableList<BackupFile>,
    val preview: (String) -> Unit,
    val restore: (String) -> Unit
) : RecyclerView.Adapter<BackupListAdapter.BackupsViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BackupsViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = RecyclerviewItemBackupsBinding.inflate(layoutInflater, parent, false)
        return BackupsViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BackupsViewHolder, position: Int) = holder.bind(filesList[position])

    inner class BackupsViewHolder(private var binding: RecyclerviewItemBackupsBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(backupFile: BackupFile) {
            binding.backupFile.text = backupFile.fileName.removeSuffix(".tar.gz").replace('-', ' ')
            binding.fileSize.text = backupFile.fileSize
            binding.viewContent.text =
                context.getString(R.string.view_backup_contents, backupFile.noOfApps)
            binding.viewContent.setOnClickListener { preview(backupFile.fileName) }
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
    }

    internal fun setFiles(files: MutableList<BackupFile>) {
        this.filesList = files
        notifyDataSetChanged()
    }

    internal fun getFileAndRemoveAt(position: Int): String {
        val current = filesList[position]
        filesList.removeAt(position)
        notifyItemRemoved(position)
        return current.fileName
    }

    override fun getItemCount() = filesList.size
}
