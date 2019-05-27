package com.gradientinsight.gallery01.entities;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

@Entity(tableName = "photo_table")
public class Photo implements Parcelable, Comparable, Cloneable {

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "id")
    public String id;

    @ColumnInfo(name = "file")
    public String file;

    @ColumnInfo(name = "name")
    public String name;

    @ColumnInfo(name = "type")
    public String type;

    @ColumnInfo(name = "date_taken")
    public String dateTaken;

    @ColumnInfo(name = "tag")
    public String tag;

    public Photo(@NonNull String id, String file, String name, String type, String dateTaken, String tag) {
        this.id = id;
        this.file = file;
        this.name = name;
        this.type = type;
        this.dateTaken = dateTaken;
        this.tag = tag;
    }

    public static class PhotoBuilder {
        private String id;
        private String file;
        private String name;
        private String type;
        private String dateTaken;
        private String tag;

        public PhotoBuilder setId(String id) {
            this.id = id;
            return this;
        }

        public PhotoBuilder setFile(String file) {
            this.file = file;
            return this;
        }

        public PhotoBuilder setName(String name) {
            this.name = name;
            return this;
        }

        public PhotoBuilder setType(String type) {
            this.type = type;
            return this;
        }

        public PhotoBuilder setDateTaken(String dateTaken) {
            this.dateTaken = dateTaken;
            return this;
        }

        public PhotoBuilder setTag(String tag) {
            this.tag = tag;
            return this;
        }

        public Photo create() {
            return new Photo(id, file, name, type, dateTaken, tag);
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public Photo(Parcel in) {
        this.id = in.readString();
        this.file = in.readString();
        this.name = in.readString();
        this.type = in.readString();
        this.dateTaken = in.readString();
        this.tag = in.readString();
    }

    @Override
    public void writeToParcel(Parcel in, int flags) {
        in.writeString(this.id);
        in.writeString(this.file);
        in.writeString(this.name);
        in.writeString(this.type);
        in.writeString(this.dateTaken);
        in.writeString(this.tag);
    }

    public static final Creator<Photo> CREATOR = new Creator<Photo>() {
        @Override
        public Photo createFromParcel(Parcel source) {
            return new Photo(source);
        }

        @Override
        public Photo[] newArray(int size) {
            return new Photo[size];
        }
    };

    @NonNull
    public String getId() {
        return id;
    }

    public String getFile() {
        return file;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String getDateTaken() {
        return dateTaken;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    @Override
    public int compareTo(Object o) {
        Photo compare = (Photo) o;
        if (compare.id.equals(this.id) && compare.file.equals(this.file) && compare.name.equals(this.name)
                && compare.type.equals(this.type) && compare.dateTaken.equals(this.dateTaken) && compare.tag.equals(this.tag)) {
            return 0;
        }
        return 1;
    }

    @Override
    public Photo clone() {
        Photo clone;
        try {
            clone = (Photo) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e); //should not happen
        }
        return clone;
    }
}
