require qat.inc

SRC_URI = "https://downloadmirror.intel.com/765523/QAT20.L.1.0.0-00021.tar.gz;subdir=qat20 \
           file://0001-qat-fix-for-cross-compilation-issue.patch \
           file://0001-qat20-remove-local-path-from-makefile.patch \
           file://0003-qat-override-CC-LD-AR-only-when-it-is-not-define.patch \
           file://0004-qat20-update-KDIR-for-cross-compilation.patch \
           file://0005-Added-include-dir-path.patch \
           file://0006-qat20-qat-add-install-target-and-add-folder.patch \
           file://0001-usdm_drv-convert-mutex_lock-to-mutex_trylock-to-avio.patch \
           file://0001-utilities-adf_ctl-Allow-CXX-to-be-overridden.patch \
          "

SRC_URI[sha256sum] = "d705492e04f633ff8494304db6166b9f3562068bf7ef3a21bdee73410413922d"

S = "${WORKDIR}/qat20"

do_install:append() {
  #install qat source
  cp ${DL_DIR}/QAT20.L.${PV}.tar.gz ${D}${prefix}/src/qat/
}

