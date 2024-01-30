import easyocr as ocr
import cv2
import numpy as np
import os
import glob


def plate_character_recognition():
    # Resmi yükle
    # image_path = 'saved_car/...jpg'  # Gerçek resim yolunu buraya girin
    latest_image = max(glob.glob('save_car/*.jpg'), key=os.path.getctime)
    # Resmi yükle
    image_path = latest_image
    img = cv2.imread(image_path)

    # Kırmızı rengin BGR değerlerini tanımla
    # Bu değerler genel bir tahmindir ve görüntüye göre ayarlanmalıdır.
    lower_red = np.array([0, 0, 100])
    upper_red = np.array([100, 100, 255])

    # Kırmızı rengi maskele
    mask = cv2.inRange(img, lower_red, upper_red)

    # Maske üzerinde konturları bul
    contours, _ = cv2.findContours(mask, cv2.RETR_EXTERNAL, cv2.CHAIN_APPROX_SIMPLE)

    # Eğer kontur varsa, en büyük konturu bul
    if contours:
        # En büyük kontur genellikle plaka çerçevesi olacaktır
        largest_contour = max(contours, key=cv2.contourArea)
        x, y, w, h = cv2.boundingRect(largest_contour)

        # Kırpma işlemi için sınırlayıcı kutunun koordinatlarını kullan
        cropped_img = img[y:y + h, x:x + w]

        ocr_motoru = ocr.Reader(['en', 'tr'])
        yazilar = ocr_motoru.readtext(cropped_img)
        
        yeni = yazilar[3]
        yaz=yeni[1]
        print(yaz)
        
        save_directory = 'saved_plates'
        os.makedirs(save_directory, exist_ok=True)

        # Kırpılmış resmi kaydet
        save_path = os.path.join(save_directory, 'cropped_plaka.jpg')  # Kaydedilecek resmin dosya adı
        cv2.imwrite(save_path, cropped_img)

        # Kırpılmış resmi göster
        cv2.imshow('Cropped Image', cropped_img)
       
        cv2.destroyAllWindows()
    return yaz






"""
if __name__ == "__main__":
    plate_character_recognition()
"""