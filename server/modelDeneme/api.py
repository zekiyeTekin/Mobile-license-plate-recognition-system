from flask import Flask, request, jsonify
from werkzeug.utils import secure_filename
import os
import traceback
import logging
from main import model

app = Flask(__name__)


# Log dosyasını ayarla (isteğe bağlı)
logging.basicConfig(filename='flask.log', level=logging.DEBUG)

UPLOAD_FOLDER = 'uploads'
ALLOWED_EXTENSIONS = {'jpg', 'jpeg', 'png', 'gif'}
app.config['UPLOAD_FOLDER'] = UPLOAD_FOLDER

def allowed_file(filename):
    return '.' in filename and filename.rsplit('.', 1)[1].lower() in ALLOWED_EXTENSIONS

@app.route('/app/upload', methods=['POST'])
def analyze_image():
    try:
        if 'file' not in request.files:
            app.logger.error('No file part')
            return jsonify({'error': 'No file part'})

        file = request.files['file']

        if file.filename == '':
            app.logger.error('No selected file')
            return jsonify({'error': 'No selected file'})

        if file and allowed_file(file.filename):
            filename = secure_filename(file.filename)
            filepath = os.path.join(app.config['UPLOAD_FOLDER'], filename)
            file.save(filepath)

            app.logger.info('File uploaded successfully')

            plaka=model()
            return jsonify({'success': True, 'message': ''+plaka})

        app.logger.error('Invalid file format')
        return jsonify({'error': 'Invalid file format'})
    
    except Exception as e:
        app.logger.error(str(e))
        # Hata durumunda traceback modülü ile hata mesajını alın
        error_traceback = traceback.format_exc()
        app.logger.error(error_traceback)
        return jsonify({'error': str(e), 'traceback': error_traceback})

if __name__ == '__main__':
    if not os.path.exists(UPLOAD_FOLDER):
        os.makedirs(UPLOAD_FOLDER)

    app.run(host='0.0.0.0', port=5000, debug=True)


