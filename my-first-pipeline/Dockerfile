# Use a lightweight, official Python runtime as the base image
FROM python:3.10-slim

# Install Java JRE (Required if Jenkins needs to manage this container directly)
RUN apt-get update && apt-get install -y --no-install-recommends \
    default-jre-headless \
    && rm -rf /var/lib/apt/lists/*

# Set the working directory inside the container
WORKDIR /app

# Copy the requirements file first to leverage Docker caching
COPY requirements.txt ./

# Install any needed Python dependencies
RUN pip install --no-cache-dir -r requirements.txt

# Copy the rest of your application code into the container
COPY . .

# Specify the default command to run your Python script
CMD ["python", "main.py"]
