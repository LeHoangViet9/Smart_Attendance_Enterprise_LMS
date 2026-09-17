from fastapi import FastAPI, UploadFile, File, Form, HTTPException
import face_recognition
import numpy as np
import cv2
import json
from typing import List, Dict, Any

app = FastAPI(title="EduFit Face Recognition AI Service")

@app.post("/api/v1/ai/extract-vector")
async def extract_vector(file: UploadFile = File(...)):
    """
    Extracts a 128-d face descriptor from an uploaded image.
    Used during student face onboarding.
    """
    try:
        # Read image file
        contents = await file.read()
        nparr = np.frombuffer(contents, np.uint8)
        img = cv2.imdecode(nparr, cv2.IMREAD_COLOR)

        if img is None:
            raise HTTPException(status_code=400, detail="Invalid image file")

        # Convert BGR to RGB (face_recognition uses RGB)
        rgb_img = cv2.cvtColor(img, cv2.COLOR_BGR2RGB)

        # Detect face locations and encodings
        face_locations = face_recognition.face_locations(rgb_img)
        if not face_locations:
            return {"success": False, "message": "No face detected in the image", "descriptor": []}
        
        if len(face_locations) > 1:
            return {"success": False, "message": "Multiple faces detected. Please upload an image with only your face.", "descriptor": []}

        face_encodings = face_recognition.face_encodings(rgb_img, face_locations)
        
        if not face_encodings:
             return {"success": False, "message": "Could not extract face features", "descriptor": []}
             
        # Return the first face's 128-d vector as a standard python list
        descriptor = face_encodings[0].tolist()
        
        return {
            "success": True,
            "message": "Face processed successfully",
            "descriptor": descriptor
        }

    except Exception as e:
        print(f"Error in extract_vector: {str(e)}")
        raise HTTPException(status_code=500, detail="Internal server error while processing face")


@app.post("/api/v1/ai/verify-face")
async def verify_face(
    file: UploadFile = File(...), 
    registered_descriptors: str = Form(...) 
):
    """
    Verifies faces in an uploaded classroom frame against a list of registered student descriptors.
    registered_descriptors: JSON string format: [{"studentId": 1, "descriptor": [0.12, 0.34, ...]}]
    Returns list of matched studentIds.
    """
    try:
        # Read image frame
        contents = await file.read()
        nparr = np.frombuffer(contents, np.uint8)
        img = cv2.imdecode(nparr, cv2.IMREAD_COLOR)

        if img is None:
            raise HTTPException(status_code=400, detail="Invalid image file")

        rgb_img = cv2.cvtColor(img, cv2.COLOR_BGR2RGB)
        
        # Detect all faces in the frame
        face_locations = face_recognition.face_locations(rgb_img)
        if not face_locations:
            return {"success": True, "present_student_ids": []}

        unknown_encodings = face_recognition.face_encodings(rgb_img, face_locations)

        # Parse registered descriptors from JSON
        try:
            registered_data = json.loads(registered_descriptors)
        except json.JSONDecodeError:
            raise HTTPException(status_code=400, detail="Invalid registered_descriptors JSON")

        known_encodings = []
        student_ids = []
        for item in registered_data:
            if 'descriptor' in item and 'studentId' in item:
                # face_recognition expects numpy array for comparison
                known_encodings.append(np.array(item['descriptor']))
                student_ids.append(item['studentId'])
                
        if not known_encodings:
             return {"success": True, "present_student_ids": []}

        present_ids = set()

        # Compare each face found in the frame with all known encodings
        for unknown_encoding in unknown_encodings:
            # Tolerance 0.6 is typical for face_recognition
            matches = face_recognition.compare_faces(known_encodings, unknown_encoding, tolerance=0.55)
            
            # Find the best match (smallest distance)
            face_distances = face_recognition.face_distance(known_encodings, unknown_encoding)
            best_match_index = np.argmin(face_distances)
            
            if matches[best_match_index]:
                present_ids.add(student_ids[best_match_index])

        return {
            "success": True,
            "present_student_ids": list(present_ids)
        }

    except Exception as e:
        print(f"Error in verify_face: {str(e)}")
        raise HTTPException(status_code=500, detail="Internal server error during verification")

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000)
