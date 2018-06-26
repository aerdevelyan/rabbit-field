import AppMain from '../../main/js/app';

test('main app test', () => {
    const app = new AppMain();
    expect(app.fld1).toBe(true);
});