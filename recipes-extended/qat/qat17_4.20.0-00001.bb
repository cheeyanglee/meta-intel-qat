require qat.inc

SRC_URI = "https://downloadmirror.intel.com/761891/QAT.L.4.20.0-00001.tar.gz;subdir=qat17 \
           file://0001-qat-fix-for-cross-compilation-issue.patch \
           file://0002-qat-remove-local-path-from-makefile.patch \
           file://0003-qat-override-CC-LD-AR-only-when-it-is-not-define.patch \
           file://0004-update-KDIR-for-cross-compilation.patch \
           file://0005-Added-include-dir-path.patch \
           file://0006-qat-add-install-target-and-add-folder.patch \
           file://0001-usdm_drv-convert-mutex_lock-to-mutex_trylock-to-avio.patch \
           file://qat-remove-the-deprecated-pci-dma-compat.h-API.patch \
          "

SRC_URI[sha256sum] = "90ca71c551cbb963b261f528616f2be5085f23a87578f2325623cfb6511a32f7"

S = "${WORKDIR}/qat17"

do_install:append() {
  install -D -m 0755 ${S}/quickassist/lookaside/access_layer/src/qat_direct/src/build/linux_2.6/user_space/libadf_user.a ${D}${base_libdir}/libadf.a

  install -m 0755 ${S}/quickassist/qat/fw/qat_d15xx.bin  ${D}${nonarch_base_libdir}/firmware
  install -m 0755 ${S}/quickassist/qat/fw/qat_d15xx_mmp.bin  ${D}${nonarch_base_libdir}/firmware

  #install qat source
  cp ${DL_DIR}/QAT.L.${PV}.tar.gz ${D}${prefix}/src/qat/
}
