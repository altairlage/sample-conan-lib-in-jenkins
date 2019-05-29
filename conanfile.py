from conans import ConanFile, tools
from datetime import datetime
import json
import os


DEPENDENCY_FILE_PATH = './dependency.json'


def _get_version_from_dependency_file():
        file_content = tools.load(DEPENDENCY_FILE_PATH)
        content = json.loads(file_content)
        return str(content['version'])


VERSION = _get_version_from_dependency_file()


class CryptoCoreConan(ConanFile):
    name = 'MyMultiOSLib'
    version = VERSION
    license = 'Copyright (C) AltaBuild, Inc - All Rights Reserved'
    url = 'https://<git code repo url>.git'
    checkout_folder = 'myMultiOSLib-git'
    description = 'This is a sample library to be built using Conan.'
    settings = 'os', 'compiler', 'build_type', 'arch'
    options = {'start_time': 'ANY', 'branch': 'ANY'}
    exports= "dependency.json"

    def source(self):
        git = tools.Git(
            folder = self.checkout_folder,
            username=os.getenv('GIT_USERNAME'),
            password=os.getenv('GIT_PASSWORD')
        )
        git.clone(url=self.url, branch=self.options.branch)

    def build(self):
        self._configure_artifact_file()
        
        if self.settings.os == 'Windows':
            self.run("msbuild /p:Configuration=Release /t:rebuild /m MyMultiOSLib-git\MyMultiOSLib.sln /p:Platform=x86")
            self.run("msbuild /p:Configuration=Release /t:rebuild /m MyMultiOSLib-git\MyMultiOSLib.sln /p:Platform=x64")
        else:
            self._execute_makefile()

    def _configure_artifact_file(self):
        tools.save('artifact.properties', '\n'.join([
            'artifact_property_build.name=my-multi-os-lib',
            'artifact_property_build.timestamp={utc_started}'.format(utc_started=self.options.start_time)])
        )

    def _execute_makefile(self):
        with tools.chdir(self.checkout_folder):
            self.run('make -f makefile-my-multi-os-lib.mk')

    def package(self):
        self.run("dir")
        self.copy('*.h', dst='include', src='.')
        self.copy('*.a', dst='lib', keep_path=False)
        
        # TODO: This is to be removed in the future when it is building just the static library instead of the dynamic libraries as well
        self.copy('*.so', dst='lib', keep_path=False)
        self.copy('*.dylib', dst='lib', keep_path=False)
        self.copy('*.dll', dst='lib/x86', src="MyMultiOSLib-git/Release", keep_path=False)
        self.copy('*.dll', dst='lib/x64', src="MyMultiOSLib-git/x64", keep_path=False)

    def package_info(self):
        self.cpp_info.libs = ['my-multi-os-lib']
