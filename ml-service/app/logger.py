import logging
import sys
import os
from datetime import datetime

LOG_DIR = "/app/logs"
os.makedirs(LOG_DIR, exist_ok=True)

def setup_logger(name="ml_service", level=logging.INFO):
    logger = logging.getLogger(name)
    logger.setLevel(level)
    
    if logger.handlers:
        return logger
    
    formatter = logging.Formatter(
        '%(asctime)s - %(name)s - %(levelname)s - %(message)s',
        datefmt='%Y-%m-%d %H:%M:%S'
    )
    
    console_handler = logging.StreamHandler(sys.stdout)
    console_handler.setFormatter(formatter)
    logger.addHandler(console_handler)
    
    log_file = os.path.join(LOG_DIR, f'{name}_{datetime.now():%Y%m%d}.log')
    file_handler = logging.FileHandler(log_file)
    file_handler.setFormatter(formatter)
    logger.addHandler(file_handler)
    
    return logger

ml_logger = setup_logger("MIA")