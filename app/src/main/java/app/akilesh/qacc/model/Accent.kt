package app.akilesh.qacc.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "accent_colors")
data class Accent(
    @PrimaryKey @ColumnInfo(name = "package_name") var pkgName: String,
    @ColumnInfo(name = "name") var name: String,
    @ColumnInfo(name = "color") var colorLight: String,
    @ColumnInfo(name = "color_dark") var colorDark: String
)
