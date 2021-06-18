import setuptools

with open("../README.md", "r") as fh:
    long_description = fh.read()

setuptools.setup(
    name="sftp2ftp",  # Replace with your own username
    version="0.0.1",
    author="Thomas Andre",
    author_email="thomasandre84@gmail.com",
    description="An example sftp to ftp reverse proxy based on paramiko",
    long_description=long_description,
    long_description_content_type="text/markdown",
    url="https://github.com/pypa/sampleproject",
    packages=setuptools.find_packages(),
    classifiers=[
        "Programming Language :: Python :: 3",
        "License :: OSI Approved :: MIT License",
        "Operating System :: OS Independent",
    ],
    python_requires='>=3.6',
    setup_requires=['pytest-runner', 'paramiko', 'rx'],
    tests_require=['pytest'],
)