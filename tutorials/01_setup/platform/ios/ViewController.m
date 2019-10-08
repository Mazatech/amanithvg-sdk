/****************************************************************************
** Copyright (C) 2004-2019 Mazatech S.r.l. All rights reserved.
**
** This file is part of AmanithVG software, an OpenVG implementation.
**
** Khronos and OpenVG are trademarks of The Khronos Group Inc.
** OpenGL is a registered trademark and OpenGL ES is a trademark of
** Silicon Graphics, Inc.
**
** This file is provided AS IS with NO WARRANTY OF ANY KIND, INCLUDING THE
** WARRANTY OF DESIGN, MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE.
**
** For any information, please contact info@mazatech.com
**
****************************************************************************/
#import "ViewController.h"
#import "View.h"

@implementation ViewController {

    View *_view;
}

- (void)viewDidLoad {

    [super viewDidLoad];

    _view = (View *)self.view;
#ifdef AM_SRE
    // set the view to use the default device
    _view.device = MTLCreateSystemDefaultDevice();
    NSAssert(_view.device, @"Metal is not supported on this device");
#endif
    // initialize the view
    [_view initView];
#ifdef AM_SRE
    _view.delegate = _view;
#endif
}

@end
