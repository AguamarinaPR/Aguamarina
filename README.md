# Aguamarina
Aguamarina is a package manager for legacy versions of android forked from Aptoide, supporting most legacy Android versions, including Android 1.0, though it will likely not work as intended on newer Android versions.
## Why?
Legacy iOS versions have a thriving community of sideloaders, with Cydia acting as the definitive package manager.

Legacy Android versions do not have a thriving community at all, with existing package managers like Aptoide, APKtor and F-Droid being difficult to create repositories for on modern computers and not being very accessible. Aguamarina aims to fix these issues and possibly help start a bigger community of legacy Android users.
## How do I make a repository?
At the moment, the easiest way is to use the [old Aptoide server script for Windows](https://web.archive.org/web/20101122200037/http://androidworld.codinghut.com/2010/08/host-an-aptoide-repository-under-windows/) with [PHP 5.3.3](https://windows.php.net/downloads/releases/archives/php-5.3.3-Win32-VC6-x86.msi) and if you can get PHP 5 for Linux running, the [old original Aptoide server script](https://web.archive.org/web/20120513212136/http://aptoide.com/srv_install.html), then modify the generated info.xml file by either of these  to suit your needs, with [Aptoide's XML definition](/docs/XML_File_definition.pdf) for reference, then upload the resulting repository's files to a server. (Internet Archive works too since the files are static)

If you cannot get either of these running, you can modify this [example info.xml file](/docs/info.xml) with [Aptoide's XML definition](/docs/XML_File_definition.pdf) for reference, then upload the resulting repository's files to a server. (Internet Archive works too since the files are static)

I know that these methods of authoring the files are tedious and inconvenient, so I hope to correct that in the future
## Disclaimers
The software is not yet available compiled as it is still being developed. If you choose to compile it yourself, I do not recommend you log into any repositories, this feature is not secure for unsupported devices and will soon be removed.