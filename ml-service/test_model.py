import pickle
import sys

try:
    with open('model.pkl', 'rb') as f:
        model = pickle.load(f)
    print('✓ Model loaded successfully')
    print(f'✓ Model type: {type(model).__name__}')
except Exception as e:
    print(f'✗ Failed to load model: {e}')
    sys.exit(1)