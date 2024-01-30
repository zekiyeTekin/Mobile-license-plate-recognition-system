import cv2
from PIL import Image
import matplotlib.pyplot as plt
from easyocr import easyocr

import util
from ultralytics import YOLO
import os
import shutil
import glob
from util import plate_character_recognition


def model():
    # Test edilecek resmin dosya yolu
    image_path = 'uploads\\image.jpg'

    # Eğittiğiniz modelin dosya yolu
    model_path = 'modelDeneme\\best.pt'

    # YOLO modelini yükle
    model = YOLO(model_path)

    # Plaka tanıma işlemi
    im1 = Image.open(image_path)
    sonuc = model.predict(source=im1, save=True)

    # Kaydedilen plakaların taşınacağı klasör
    save_directory = 'save_car'
    os.makedirs(save_directory, exist_ok=True)

    # Mevcut kayıt sayısını hesapla
    kayit_sirasi = len(os.listdir(save_directory)) + 1

    # Kaydedilen görüntülerin bulunduğu son 'predict' klasörünü bul
    predict_folders = sorted(glob.glob('runs/detect/predict*'), key=os.path.getmtime)
    last_predict_folder = predict_folders[-1] if predict_folders else None

    # Eğer son 'predict' klasörü varsa, içindeki görüntüleri 'saved_plates' klasörüne taşı
    if last_predict_folder:
        for saved_image in glob.glob(f'{last_predict_folder}/*.jpg'):
            new_filename = f'arac_{kayit_sirasi}.jpg'
            new_file_path = os.path.join(save_directory, new_filename)
            shutil.move(saved_image, new_file_path)
            kayit_sirasi += 1

        print(f"Plakalar {save_directory} klasörüne taşındı.")
    else:
        print("Kaydedilen plaka görüntüsü bulunamadı.")

    sekiz=util.plate_character_recognition()
    return sekiz
