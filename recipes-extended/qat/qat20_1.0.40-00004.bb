DESCRIPTION = "Intel(r) QuickAssist Technology API"
HOMEPAGE = "https://www.intel.com/content/www/us/en/developer/topic-technology/open/quick-assist-technology/overview.html"

#Dual BSD and GPLv2 License
LICENSE = "BSD-3-Clause & GPL-2.0-only"
LIC_FILES_CHKSUM = "\
                    file://LICENSE.GPL;md5=751419260aa954499f7abaabaa882bbe \
                    file://LICENSE.BSD;md5=4a6a5cd99f6064d61adad8c6c0bd080f \
                    "
DEPENDS += "boost udev zlib openssl yasm-native"
PROVIDES += "virtual/qat"

SRC_URI = "https://downloadmirror.intel.com/781387/QAT20.L.1.0.40-00004.tar.gz;subdir=qat20 \
           file://0001-qat-fix-for-cross-compilation-issue.patch \
           file://0001-qat20-remove-local-path-from-makefile.patch \
           file://0003-qat-override-CC-LD-AR-only-when-it-is-not-define.patch \
           file://0004-qat20-update-KDIR-for-cross-compilation.patch \
           file://0005-Added-include-dir-path.patch \
           file://0006-qat20-qat-add-install-target-and-add-folder.patch \
           file://0001-usdm_drv-convert-mutex_lock-to-mutex_trylock-to-avio.patch \
           file://0001-utilities-adf_ctl-Allow-CXX-to-be-overridden.patch \
          "

SRC_URI[sha256sum] = "34f7556aaf4568eed2489d2764e89597b25e37f051a0bf5b0646a94b31fcd963"

S = "${WORKDIR}/qat20"

TARGET_CC_ARCH += "${LDFLAGS}"

do_configure[depends] += "virtual/kernel:do_shared_workdir"

COMPATIBLE_MACHINE = "null"
COMPATIBLE_HOST:x86-x32 = 'null'
COMPATIBLE_HOST:libc-musl:class-target = 'null'

ICP_TOOLS = "accelcomp"
SAMPLE_CODE_DIR = "${S}/quickassist/lookaside/access_layer/src/sample_code"
QAT_HEADER_FILES = "/opt/intel/QAT/quickassist"
HUGE_PAGE_DIR = "/dev/hugepages/qat"

export INSTALL_MOD_PATH = "${D}"
export ICP_ROOT = "${S}"
export ICP_ENV_DIR = "${S}/quickassist/build_system/build_files/env_files"
export ICP_BUILDSYSTEM_PATH = "${S}/quickassist/build_system"
export ICP_TOOLS_TARGET = "${ICP_TOOLS}"
export FUNC_PATH = "${ICP_ROOT}/quickassist/lookaside/access_layer/src/sample_code/functional"
export INSTALL_FW_PATH = "${D}${base_libdir}/firmware"
export KERNEL_SOURCE_ROOT = "${STAGING_KERNEL_DIR}"
export ICP_BUILD_OUTPUT = "${D}"
export DEST_LIBDIR = "${libdir}"
export DEST_BINDIR = "${bindir}"
export QAT_KERNEL_VER = "${KERNEL_VERSION}"
export SAMPLE_BUILD_OUTPUT = "${D}"
export INSTALL_MOD_DIR = "${D}${base_libdir}/modules/${KERNEL_VERSION}"
export KERNEL_BUILDDIR = "${STAGING_KERNEL_BUILDDIR}"
export SC_EPOLL_DISABLED = "1"
export WITH_UPSTREAM = "1"
export WITH_CMDRV = "1"
export KERNEL_SOURCE_DIR = "${ICP_ROOT}/quickassist/qat/"
export ICP_NO_CLEAN = "1"
export ICP_QDM_IOMMU = "1"

inherit module
inherit update-rc.d
INITSCRIPT_NAME = "qat_service"

PARALLEL_MAKE = ""

EXTRA_OEMAKE:append = " CFLAGS+='-fgnu89-inline -fPIC'"
EXTRA_OEMAKE = "-e MAKEFLAGS="

do_compile () {
  export LD="${LD} --hash-style=gnu"
  export MACHINE="${TARGET_ARCH}"

  cd ${S}/quickassist/qat
  oe_runmake
  oe_runmake 'modules_install'

  cd ${S}/quickassist
  oe_runmake

  cd ${S}/quickassist/utilities/adf_ctl
  oe_runmake

  cd ${S}/quickassist/utilities/libusdm_drv
  oe_runmake

  cd ${S}/quickassist/lookaside/access_layer/src/qat_direct/src/
  oe_runmake

  #build the whole sample code: per_user only
  cd ${SAMPLE_CODE_DIR}
  oe_runmake 'perf_user'
}

do_install() {
  export MACHINE="${TARGET_ARCH}"

  cd ${S}/quickassist
  oe_runmake install

  cd ${S}/quickassist/qat
  oe_runmake modules_install

  install -d ${D}${sysconfdir}/udev/rules.d
  install -d ${D}${sbindir}
  install -d ${D}${sysconfdir}/conf_files
  install -d ${D}${prefix}/src/qat

  echo 'KERNEL=="qat_adf_ctl" MODE="0660" GROUP="qat"' > ${D}/etc/udev/rules.d/00-qat.rules
  echo 'KERNEL=="qat_dev_processes" MODE="0660" GROUP="qat"' >> ${D}/etc/udev/rules.d/00-qat.rules
  echo 'KERNEL=="usdm_drv" MODE="0660" GROUP="qat"' >> ${D}/etc/udev/rules.d/00-qat.rules
  echo 'KERNEL=="uio*" MODE="0660" GROUP="qat"' >> ${D}/etc/udev/rules.d/00-qat.rules
  echo 'ACTION=="add", DEVPATH=="/module/usdm_drv" SUBSYSTEM=="module" RUN+="/bin/mkdir ${HUGE_PAGE_DIR}"' >> ${D}/etc/udev/rules.d/00-qat.rules
  echo 'ACTION=="add", DEVPATH=="/module/usdm_drv" SUBSYSTEM=="module" RUN+="/bin/chgrp qat ${HUGE_PAGE_DIR}"' >> ${D}/etc/udev/rules.d/00-qat.rules
  echo 'ACTION=="add", DEVPATH=="/module/usdm_drv" SUBSYSTEM=="module" RUN+="/bin/chmod 0770 ${HUGE_PAGE_DIR}"' >> ${D}/etc/udev/rules.d/00-qat.rules
  echo 'ACTION=="remove", DEVPATH=="/module/usdm_drv" SUBSYSTEM=="module" RUN+="/bin/rmdir ${HUGE_PAGE_DIR}"' >> ${D}/etc/udev/rules.d/00-qat.rules

  mkdir -p ${D}${base_libdir}

  install -D -m 0755 ${S}/quickassist/lookaside/access_layer/src/build/linux_2.6/user_space/libqat_s.so ${D}${base_libdir}
  install -D -m 0755 ${S}/quickassist/lookaside/access_layer/src/build/linux_2.6/user_space/libqat.a ${D}${base_libdir}
  install -D -m 0755 ${S}/quickassist/utilities/osal/src/build/linux_2.6/user_space/libosal_s.so ${D}${base_libdir}
  install -D -m 0755 ${S}/quickassist/utilities/osal/src/build/linux_2.6/user_space/libosal.a ${D}${base_libdir}
  install -D -m 0755 ${S}/quickassist/utilities/libusdm_drv/libusdm_drv_s.so ${D}${base_libdir}
  install -D -m 0755 ${S}/quickassist/utilities/libusdm_drv/libusdm_drv.a ${D}${base_libdir}
  install -D -m 0750 ${S}/quickassist/utilities/adf_ctl/adf_ctl ${D}${sbindir}

  install -D -m 640 ${S}/quickassist/utilities/adf_ctl/conf_files/*.conf  ${D}${sysconfdir}/conf_files
  install -D -m 640 ${S}/quickassist/utilities/adf_ctl/conf_files/*.conf.vm  ${D}${sysconfdir}/conf_files

  install -d ${D}${QAT_HEADER_FILES}/include
  install -d ${D}${QAT_HEADER_FILES}/include/dc
  install -d ${D}${QAT_HEADER_FILES}/include/lac
  install -d ${D}${QAT_HEADER_FILES}/lookaside/access_layer/include
  install -d ${D}${QAT_HEADER_FILES}/utilities/libusdm_drv

  install -m 0755 ${S}/quickassist/qat/fw/qat_*.bin  ${D}${nonarch_base_libdir}/firmware

  install -m 640 ${S}/quickassist/include/*.h  ${D}${QAT_HEADER_FILES}/include
  install -m 640 ${S}/quickassist/include/dc/*.h  ${D}${QAT_HEADER_FILES}/include/dc
  install -m 640 ${S}/quickassist/include/lac/*.h  ${D}${QAT_HEADER_FILES}/include/lac
  install -m 640 ${S}/quickassist/lookaside/access_layer/include/*.h  ${D}${QAT_HEADER_FILES}/lookaside/access_layer/include
  install -m 640 ${S}/quickassist/utilities/libusdm_drv/*.h  ${D}${QAT_HEADER_FILES}/utilities/libusdm_drv

  install -m 0755 ${S}/quickassist/lookaside/access_layer/src/sample_code/performance/compression/calgary  ${D}${nonarch_base_libdir}/firmware
  install -m 0755 ${S}/quickassist/lookaside/access_layer/src/sample_code/performance/compression/calgary32  ${D}${nonarch_base_libdir}/firmware
  install -m 0755 ${S}/quickassist/lookaside/access_layer/src/sample_code/performance/compression/canterbury  ${D}${nonarch_base_libdir}/firmware

  #install qat source
  cp ${DL_DIR}/QAT20.L.${PV}.tar.gz ${D}${prefix}/src/qat/
}

SYSROOT_DIRS += "/opt"

PACKAGES += "${PN}-app"

FILES:${PN}-dev = "${QAT_HEADER_FILES}/ \
                   ${nonarch_base_libdir}/*.a \
                   "

FILES:${PN} += "\
                ${libdir}/ \
                ${nonarch_base_libdir}/firmware \
                ${sysconfdir}/ \
                ${sbindir}/ \
                ${base_libdir}/*.so \
                ${prefix}/src/qat \
                "

FILES:${PN}-dbg += "${sysconfdir}/init.d/.debug/ \
                    "

FILES:${PN}-app += "${bindir}/* \
                    ${prefix}/qat \
                    "

# yasm encodes path to the input file and doesn't provide any option to workaround it.
INSANE_SKIP:${PN}-staticdev += "buildpaths"
INSANE_SKIP:${PN}-dbg += "buildpaths"
