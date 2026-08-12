
from PIL import Image
import numpy as np

img1 = Image.open('app/src/main/res/drawable/ic_vai_logo.png')
img2 = Image.open('app/src/main/res/drawable/ic_app_logo.png')

print('img1 mode:', img1.mode, 'size:', img1.size)
print('img2 mode:', img2.mode, 'size:', img2.size)

arr1 = np.array(img1)
arr2 = np.array(img2)

print('img1 min max per channel:', arr1.min(axis=(0,1)), arr1.max(axis=(0,1)))
print('img2 min max per channel:', arr2.min(axis=(0,1)), arr2.max(axis=(0,1)))

print('Are img1 and img2 identical?', np.array_equal(arr1, arr2))
