package no.nordicsemi.android.support.v18.scanner;

import android.os.ParcelUuid;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;
import java.util.UUID;

final class ScanRecordMatchers {

    /**
     * Check if the uuid pattern is contained in a list of parcel uuids.
     */
    static boolean matchesServiceUuids(@Nullable final ParcelUuid uuid,
                                       @Nullable final ParcelUuid parcelUuidMask,
                                       @Nullable final List<ParcelUuid> uuids) {
        if (uuid == null) {
            return true;
        }
        if (uuids == null) {
            return false;
        }

        for (final ParcelUuid parcelUuid : uuids) {
            final UUID uuidMask = parcelUuidMask == null ? null : parcelUuidMask.getUuid();
            if (matchesServiceUuid(uuid.getUuid(), uuidMask, parcelUuid.getUuid())) {
                return true;
            }
        }
        return false;
    }

    // Check if the uuid pattern matches the particular service uuid.
    static boolean matchesServiceUuid(@NonNull final UUID uuid,
                                      @Nullable final UUID mask,
                                      @NonNull final UUID data) {
        if (mask == null) {
            return uuid.equals(data);
        }
        if ((uuid.getLeastSignificantBits() & mask.getLeastSignificantBits()) !=
                (data.getLeastSignificantBits() & mask.getLeastSignificantBits())) {
            return false;
        }
        return ((uuid.getMostSignificantBits() & mask.getMostSignificantBits()) ==
                (data.getMostSignificantBits() & mask.getMostSignificantBits()));
    }

    // Check whether the data pattern matches the parsed data.
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    static boolean matchesPartialData(@Nullable final byte[] data,
                                      @Nullable final byte[] dataMask,
                                      @Nullable final byte[] parsedData) {
        if (data == null) {
            // If filter data is null it means it doesn't matter.
            // We return true if any data matching the manufacturerId were found.
            return parsedData != null;
        }
        if (parsedData == null || parsedData.length < data.length) {
            return false;
        }
        if (dataMask == null) {
            for (int i = 0; i < data.length; ++i) {
                if (parsedData[i] != data[i]) {
                    return false;
                }
            }
            return true;
        }
        for (int i = 0; i < data.length; ++i) {
            if ((dataMask[i] & parsedData[i]) != (dataMask[i] & data[i])) {
                return false;
            }
        }
        return true;
    }
}
