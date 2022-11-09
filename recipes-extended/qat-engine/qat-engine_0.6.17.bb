SUMMARY = "Intel® QuickAssist Technology(QAT) OpenSSL* Engine"

DESCRIPTION = "Intel® QuickAssist Technology OpenSSL* Engine (QAT_Engine) \
supports acceleration for both hardware as well as optimized software based on vectorized instructions."

HOMEPAGE = "https://github.com/intel/QAT_Engine"

LICENSE = "BSD-3-Clause & GPL-2.0-or-later"
LIC_FILES_CHKSUM = "file://LICENSE;md5=45ebf4c80a1695fbde1332d52ec3172e \
                    file://qat/LICENSE.GPL;md5=751419260aa954499f7abaabaa882bbe \
"
LICENSE:${PN} = "BSD-3-Clause"
LICENSE:${PN}-dev = "BSD-3-Clause"
LICENSE:${PN}-dbg = "BSD-3-Clause"
LICENSE:${PN}-cfg = "GPL-2.0-or-later"

SRC_URI = "git://github.com/intel/QAT_Engine.git;protocol=https;branch=master; \
           file://0001-configure.ac-remove-AC_CHECK_FILE.patch \
           file://0002-configure.ac-remove-rpath.patch \
"

SRCREV = "c1a7a965ce450afec74d490d0a6f7d382a1126aa"

DEPENDS = "qat17 openssl"
DEPENDS += "${@bb.utils.contains('PACKAGECONFIG', 'intel-crypto-mb', 'intel-crypto-mb', '', d)}"

RDEPENDS:${PN} += "${@bb.utils.contains('PACKAGECONFIG', 'intel-crypto-mb', 'intel-crypto-mb', '', d)}"

inherit pkgconfig autotools-brokensep

S = "${WORKDIR}/git"

# acceleration device (dh895xcc or c6xx or c3xxx)
QAT_DEVICE ??= "c3xxx"
QAT_DEVICE_CONFIG ??= "multi_thread_optimized"

PACKAGECONFIG ??= ""
PACKAGECONFIG[intel-crypto-mb] = "--enable-qat_sw --with-qat_sw_crypto_mb_install_dir=${STAGING_LIBDIR},,intel-crypto-mb"

export oldincludedir="${STAGING_INCDIR}"

EXTRA_OECONF += " --with-qat_hw_dir=${STAGING_DIR_HOST}/opt/intel/QAT/ "
EXTRA_OECONF += " --with-openssl-install-dir=${STAGING_DIR_HOST}/usr/ "

PACKAGES =+ " ${PN}-cfg "

FILES:${PN} += "/usr/lib64/engines-*/*"
FILES:${PN}-cfg += "${sysconfdir}/*.conf"

do_configure:prepend() {
    cd ${S}
    ./autogen.sh
}

do_install(){
    oe_runmake  with_openssl_install_dir="${D}/usr/" with_qat_hw_dir="${D}/opt/intel/QAT/" install
    if [ -n "${QAT_DEVICE}" ] && [ -n "${QAT_DEVICE_CONFIG}" ]; then
        install -d ${D}${sysconfdir}
        install -D ${S}/qat/config/${QAT_DEVICE}/${QAT_DEVICE_CONFIG}/*.conf ${D}${sysconfdir}
    fi
}

