DESCRIPTION = "QATzip is a user space library built on top of the Intel QAT user space library, \
to offload the compression and decompression requests to the Intel Chipset Series."

HOMEPAGE = "https://github.com/intel/QATzip"

LICENSE = "BSD-3-Clause & GPL-2.0-only"
LIC_FILES_CHKSUM = "\
                   file://LICENSE;md5=e499c81a76f6663c808889cc77414866 \
                   file://${COMMON_LICENSE_DIR}/GPL-2.0-only;md5=801f80980d171dd6425610833a22dbe6 \
"
SRC_URI = "git://github.com/intel/QATzip;protocol=https;branch=master \
           file://remove-rpath.patch \
"

SRCREV = "a30e179b2d495924f0a52b8cf113fa00ffe5df34"

DEPENDS += "qat17 util-linux-native lz4 "

PV = "1.0.9+git${SRCPV}"

export ICP_ROOT = "${STAGING_DIR_TARGET}/opt/intel/QAT"
export QZ_ROOT = "${S}"

# static library required to compile qzip and test
DISABLE_STATIC = ""

COMPATIBLE_MACHINE = "null"

inherit autotools-brokensep

S = "${WORKDIR}/git"

do_configure:prepend() {
   cd ${S}
   ./autogen.sh
}

do_compile() {
  oe_runmake all
}

do_install:append() {
  install -d ${D}${sysconfdir}/QATzip/conf_file
  cp -r ${S}/config_file/* ${D}${sysconfdir}/QATzip/conf_file
}
