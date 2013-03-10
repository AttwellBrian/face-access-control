import os
dirname = "./Subject03/"
for v in range(2,10):
  dirname = dirname = "./Subject0"+str(v)+"/"
  for filename in os.listdir(dirname):
    if filename.endswith("-05.Jpg") or filename.endswith("-15.Jpg"):
      os.rename(dirname+filename, "./named-training-data/"+filename)

dirname = "./named-training-data/"
for filename in os.listdir(dirname):
  os.rename(os.path.join(dirname, filename), os.path.join(dirname, str.lower(filename)))
  os.rename(
                os.path.join(dirname, filename),
                os.path.join(dirname, filename.replace('-', ''))
            )