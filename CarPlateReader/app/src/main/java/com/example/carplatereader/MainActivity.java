package com.example.carplatereader;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonObject;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private static final int CAMERA_PERMISSION_REQUEST = 100;
    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int CAMERA_REQUEST = 101;
    private ImageView imageView;
    private Button cameraBtn;
    private Button selectPhotoButton;
    private TextView plaka ;
    private Button uploadButton;
    private static final int REQUEST_CODE = 1;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = findViewById(R.id.displayImageView);
        cameraBtn = findViewById(R.id.cameraBtn);
        selectPhotoButton = findViewById(R.id.selectPhotoButton);
        plaka = findViewById((R.id.plaka));
        plaka.bringToFront();
        uploadButton = findViewById(R.id.uploadButton);



        cameraBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkCameraPermission();
            }
        });
        selectPhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
            }
        });
        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadImage();
            }
        });
    }
    private void openGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent, PICK_IMAGE_REQUEST);
    }
    private void uploadImage() {
        if (imageView.getDrawable() != null) {
            Bitmap bitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();

            // Bitmap'i byte dizisine çevir
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
            byte[] byteArray = byteArrayOutputStream.toByteArray();

            // Dosya adını belirle
            String fileName = "image.jpg";

            // MultipartBody.Part oluştur
            RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), byteArray);
            MultipartBody.Part filePart = MultipartBody.Part.createFormData("file", fileName, requestFile);

            // Sunucuya dosyayı gönder
            ApiService apiService = RetrofitClient.getRetrofitInstance().create(ApiService.class);
            Call<JsonObject> call = apiService.uploadImage(filePart);

            call.enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    if (response.isSuccessful()) {
                        // Başarılı durum
                        JsonObject result = response.body();
                        Log.d("RetrofitResponse", "Server Response: " + result);
                        Toast.makeText(MainActivity.this, "Başarılı", Toast.LENGTH_SHORT).show();
                        try {
                            // JsonObject'u String'e çevir
                            String jsonString = result.toString();

                            // String'i JSONObject'e çevir
                            JSONObject resultJSONObject = new JSONObject(jsonString);

                            // "message" alanını alın
                            String message = resultJSONObject.getString("message");

                            // Alınan "message" değerini kullan
                            plaka.setText(message);

                        } catch (JSONException e) {
                            // JSON işleme hatası durumunda buraya düşer
                            e.printStackTrace();
                        }
                    } else {
                        // Hata durumu
                        String errorMessage = "Hata: " + response.message();
                        Log.e("RetrofitError", errorMessage);
                        Toast.makeText(MainActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {
                    Log.e("RetrofitError", "Bağlantı Hatası", t);
                    Toast.makeText(MainActivity.this, "Bağlantı Hatası", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(this, "Lütfen bir fotoğraf seçin", Toast.LENGTH_SHORT).show();
        }
    }

    private void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    CAMERA_PERMISSION_REQUEST);
        } else {
            openCamera();
        }
    }

    private void openCamera() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(cameraIntent, CAMERA_REQUEST);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_REQUEST && resultCode == RESULT_OK) {
            Bitmap photo = (Bitmap) data.getExtras().get("data");

            // Fotoğrafı düzeltmek için Matrix kullanımı
            Matrix matrix = new Matrix();
            matrix.postRotate(360); // Döndürme açısını belirleyin (90 derece)
            Bitmap rotatedPhoto = Bitmap.createBitmap(photo, 0, 0, photo.getWidth(), photo.getHeight(), matrix, true);

            imageView.setImageBitmap(rotatedPhoto);
            saveImageToGallery(rotatedPhoto);
        }

        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            // Seçilen fotoğrafın URI'sini alın
            Uri uri = data.getData();

            // Fotoğrafı ImageView'e yükleyin
            try {
                Bitmap selectedPhoto = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);

                // Fotoğrafı düzeltmek için Matrix kullanımı
                Matrix matrix = new Matrix();
                matrix.postRotate(360); // Döndürme açısını belirleyin (90 derece)
                Bitmap rotatedSelectedPhoto = Bitmap.createBitmap(selectedPhoto, 0, 0, selectedPhoto.getWidth(), selectedPhoto.getHeight(), matrix, true);

                imageView.setImageBitmap(rotatedSelectedPhoto);
                /*saveImageToGallery(rotatedSelectedPhoto);*/
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void saveImageToGallery(Bitmap bitmap) {
        // Önce bir dosya oluşturun ve içine bitmap'i kaydedin
        String fileName = "Image_" + System.currentTimeMillis() + ".png";
        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), fileName);

        try {
            FileOutputStream fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.close();

            // Medya tarayıcısına bildirin ki yeni bir dosya ekledik
            sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)));

            // Kullanıcıya geri bildirim gösterin
            Toast.makeText(this, "Fotoğraf galeriye kaydedildi. Dosya Yolu: " + file.getAbsolutePath(), Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Fotoğraf galeriye kaydedilemedi.", Toast.LENGTH_SHORT).show();
        }
    }

    private String getRealPathFromURI(Uri uri) {
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);

        if (cursor == null) {
            return null;
        }

        int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String filePath = cursor.getString(columnIndex);
        cursor.close();

        return filePath;
    }

}
