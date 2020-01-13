package app.akilesh.qacc.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "accent_colors")
data class Accent(
    @PrimaryKey @ColumnInfo(name = "package_name") val pkgName: String,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "color") val colorLight: String,
    @ColumnInfo(name = "color_dark") val colorDark: String
)
