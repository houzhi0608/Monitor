package github.leavesc.monitor.db

import androidx.room.TypeConverter
import java.util.*

/**
 * 作者：leavesC
 * 时间：2019/11/7 14:57
 * 描述：
 */
internal class Converters {

    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return if (value == null) null else Date(value)
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }

}