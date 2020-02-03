package app.akilesh.qacc.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.recyclerview.widget.RecyclerView
import app.akilesh.qacc.R
import com.google.android.material.textview.MaterialTextView

class BackupListAdapter internal constructor(
    context: Context,
    private var filesList: MutableList<String>,
    val onClick : (String) -> Unit
) : RecyclerView.Adapter<BackupListAdapter.BackupsViewHolder>() {

    private val inflater: LayoutInflater = LayoutInflater.from(context)

    inner class BackupsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val file: MaterialTextView = itemView.findViewById(R.id.backup_file)
        val viewContents: AppCompatImageView = itemView.findViewById(R.id.view_content)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BackupsViewHolder {
        val itemView = inflater.inflate(R.layout.recyclerview_item_backups, parent, false)
        return BackupsViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: BackupsViewHolder, position: Int) {
        val file = filesList[position]
        holder.file.text = file.removeSuffix(".tar.gz").replace('-', ' ')
        holder.viewContents.setOnClickListener { onClick(file) }
    }

    internal fun setFiles(files: MutableList<String>) {
        this.filesList = files
        notifyDataSetChanged()
    }

    internal fun getFileAndRemoveAt(position: Int): String {
        val current = filesList[position]
        filesList.removeAt(position)
        notifyItemRemoved(position)
        return current
    }

    override fun getItemCount() = filesList.size
}
