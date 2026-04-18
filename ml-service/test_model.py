import pickle
import sys
import os

print(f"Current directory: {os.getcwd()}")
print(f"File exists: {os.path.exists('model.pkl')}")
print(f"File size: {os.path.getsize('model.pkl')} bytes")

try:
    with open('model.pkl', 'rb') as f:
        first_bytes = f.read(10)
        print(f"First bytes: {first_bytes.hex()}")
        f.seek(0) 
        
        model = pickle.load(f)
    
    print('✓ Model loaded successfully')
    print(f'✓ Model type: {type(model).__name__}')
    sys.exit(0)
except Exception as e:
    print(f'✗ Failed to load model: {e}')
    import traceback
    traceback.print_exc()
    sys.exit(1)