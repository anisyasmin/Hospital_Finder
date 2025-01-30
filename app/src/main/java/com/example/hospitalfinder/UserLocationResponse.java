package com.example.hospitalfinder;

import com.google.gson.annotations.SerializedName;

public class UserLocationResponse {

    @SerializedName("success")
    private boolean success;

    @SerializedName("data")
    private UserData data;

    public boolean isSuccess() {
        return success;
    }

    public UserData getData() {
        return data;
    }

    public static class UserData {

        @SerializedName("first_name")
        private String firstName;

        @SerializedName("last_name")
        private String lastName;

        @SerializedName("district")
        private String district;

        @SerializedName("latitude")
        private double latitude;

        @SerializedName("longitude")
        private double longitude;

        @SerializedName("hospital_count")
        private int hospitalCount;

        @SerializedName("pharmacy_count")
        private int pharmacyCount;

        public String getFirstName() {
            return firstName;
        }

        public String getLastName() {
            return lastName;
        }

        public String getDistrict() {
            return district;
        }

        public double getLatitude() {
            return latitude;
        }

        public double getLongitude() {
            return longitude;
        }

        public int getHospitalCount() {
            return hospitalCount;
        }

        public int getPharmacyCount() {
            return pharmacyCount;
        }
    }
}
