import android.os.Parcel
import android.os.Parcelable
import java.io.Serializable

data class Complaint(
    var id: Int = 0,
    val lastName: String,
    val firstName: String,
    val middleName: String?,
    val housingType: String,
    val address: String,
    val complaintText: String
) : Parcelable, Serializable {

    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString(),
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: ""
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)  // Записываем id в Parcel
        parcel.writeString(lastName)
        parcel.writeString(firstName)
        parcel.writeString(middleName)
        parcel.writeString(housingType)
        parcel.writeString(address)
        parcel.writeString(complaintText)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Complaint> {
        override fun createFromParcel(parcel: Parcel): Complaint {
            return Complaint(parcel)
        }

        override fun newArray(size: Int): Array<Complaint?> {
            return arrayOfNulls(size)
        }
    }
}
