require qat-engine-common.inc

LICENSE = "GPL-2.0-or-later"
LIC_FILES_CHKSUM = "file://LICENSE.GPL;md5=751419260aa954499f7abaabaa882bbe"

SRC_URI += " file://0001-Makefile-skip-imacros-and-test.patch \
             file://0001-Makefile-add-modules_install-target.patch \
"

DEPENDS = "virtual/kernel"

inherit module kernel-module-split

EXTRA_OEMAKE += " KDIR='${STAGING_KERNEL_DIR}' "

S = "${WORKDIR}/git/qat_contig_mem"

MODULE_NAME = "qat_contig_mem"

